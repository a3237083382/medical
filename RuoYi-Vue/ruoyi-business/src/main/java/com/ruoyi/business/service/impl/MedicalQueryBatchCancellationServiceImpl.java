package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.medical.MedicalQueryBatchCancellationResult;
import com.ruoyi.business.domain.medical.MedicalQueryBatchItemState;
import com.ruoyi.business.domain.medical.MedicalQueryBatchProgress;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.service.IMedicalQueryBatchCancellationService;
import com.ruoyi.business.service.MedicalQueryException;

@Service
public class MedicalQueryBatchCancellationServiceImpl implements IMedicalQueryBatchCancellationService
{
    private final BizMedicalQueryBatchMapper batchMapper;
    private final BizInsuranceCompanyMapper companyMapper;
    private final BizMonthlyUsageMapper monthlyUsageMapper;
    private final TransactionTemplate transactionTemplate;

    public MedicalQueryBatchCancellationServiceImpl(BizMedicalQueryBatchMapper batchMapper,
            BizInsuranceCompanyMapper companyMapper, BizMonthlyUsageMapper monthlyUsageMapper,
            PlatformTransactionManager transactionManager)
    {
        this.batchMapper = batchMapper;
        this.companyMapper = companyMapper;
        this.monthlyUsageMapper = monthlyUsageMapper;
        this.transactionTemplate = transactionManager == null ? null : new TransactionTemplate(transactionManager);
    }

    @Override
    public MedicalQueryBatchProgress getProgress(Long companyId, String batchNo)
    {
        validateBatchIdentity(companyId, batchNo);
        MedicalQueryBatchProgress progress = batchMapper.selectCompanyBatchProgress(companyId, batchNo.trim());
        if (progress == null)
        {
            throw new MedicalQueryException("4042", "batch not found");
        }
        return progress;
    }

    @Override
    public MedicalQueryBatchCancellationResult cancelBatch(Long companyId, String batchNo)
    {
        validateBatchIdentity(companyId, batchNo);
        return executeInTransaction(() -> cancelBatchInTransaction(companyId, batchNo.trim()));
    }

    @Override
    public MedicalQueryBatchCancellationResult cancelItem(Long companyId, Long itemId)
    {
        if (companyId == null || itemId == null)
        {
            throw new MedicalQueryException("4000", "companyId and itemId are required");
        }
        return executeInTransaction(() -> cancelItemInTransaction(companyId, itemId));
    }

    private MedicalQueryBatchCancellationResult cancelBatchInTransaction(Long companyId, String batchNo)
    {
        BizMedicalQueryBatch batch = batchMapper.selectCompanyBatchByNoForUpdate(companyId, batchNo);
        if (batch == null)
        {
            throw new MedicalQueryException("4042", "batch not found");
        }
        BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyById(companyId);
        List<MedicalQueryBatchItemState> items = batchMapper.selectBatchItemsForUpdate(batch.getId());
        int cancelled = 0;
        int notCancellable = 0;
        for (MedicalQueryBatchItemState item : items)
        {
            if (!"ACTIVE".equals(item.getItemStatus()))
            {
                continue;
            }
            if (!isRequestCancellable(item))
            {
                notCancellable++;
                continue;
            }
            cancelCancellableItem(item, company);
            cancelled++;
        }
        if (cancelled == 0 && notCancellable > 0)
        {
            throw new MedicalQueryException("4092", "no pending item can be cancelled");
        }
        return buildResult(companyId, batchNo, cancelled, notCancellable);
    }

    private MedicalQueryBatchCancellationResult cancelItemInTransaction(Long companyId, Long itemId)
    {
        MedicalQueryBatchItemState item = batchMapper.selectCompanyBatchItemForUpdate(companyId, itemId);
        if (item == null)
        {
            throw new MedicalQueryException("4042", "batch item not found");
        }
        if (!"ACTIVE".equals(item.getItemStatus()) || !isRequestCancellable(item))
        {
            throw new MedicalQueryException("4092", "item cannot be cancelled");
        }
        BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyById(companyId);
        cancelCancellableItem(item, company);
        return buildResult(companyId, item.getBatchNo(), 1, 0);
    }

    private void cancelCancellableItem(MedicalQueryBatchItemState item, BizInsuranceCompany company)
    {
        requireUpdated(batchMapper.cancelBatchItem(item.getItemId()), "cancel batch item");
        if ("1".equals(item.getReusedFlag()) || !"BATCH".equals(item.getEntryType())
                || batchMapper.countActiveItemsByRequestId(item.getRequestId()) > 0)
        {
            return;
        }
        requireUpdated(batchMapper.cancelPendingRequest(item.getRequestId()), "cancel medical query request");
        if (isMonthlyBudgetEnabled(company) && nvl(item.getReservedFee()).signum() > 0)
        {
            requireUpdated(monthlyUsageMapper.releaseBudget(company.getId(), item.getBillingMonth(),
                    item.getReservedFee()), "release cancelled query reservation");
        }
    }

    private MedicalQueryBatchCancellationResult buildResult(Long companyId, String batchNo, int cancelled,
            int notCancellable)
    {
        MedicalQueryBatchProgress progress = batchMapper.selectCompanyBatchProgress(companyId, batchNo);
        if (progress == null)
        {
            throw new MedicalQueryException("4042", "batch not found");
        }
        requireUpdated(batchMapper.updateBatchSummary(progress), "refresh medical query batch");
        MedicalQueryBatchCancellationResult result = new MedicalQueryBatchCancellationResult();
        result.setCancelledCount(cancelled);
        result.setNotCancellableCount(notCancellable);
        result.setProgress(progress);
        return result;
    }

    private boolean isRequestCancellable(MedicalQueryBatchItemState item)
    {
        return "PENDING".equals(item.getProcessStatus()) && "NOT_UPLOADED".equals(item.getUploadStatus());
    }

    private boolean isMonthlyBudgetEnabled(BizInsuranceCompany company)
    {
        return company != null && "0".equals(company.getBudgetEnabled()) && company.getMonthlyBudget() != null;
    }

    private void validateBatchIdentity(Long companyId, String batchNo)
    {
        if (companyId == null || batchNo == null || batchNo.trim().isEmpty())
        {
            throw new MedicalQueryException("4000", "companyId and batchNo are required");
        }
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void requireUpdated(int rows, String operation)
    {
        if (rows != 1)
        {
            throw new IllegalStateException("Failed to " + operation);
        }
    }

    private <T> T executeInTransaction(Supplier<T> action)
    {
        return transactionTemplate == null ? action.get() : transactionTemplate.execute(status -> action.get());
    }
}
