package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.BizMedicalQueryBatchItem;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.MedicalQueryBatchPreview;
import com.ruoyi.business.domain.medical.MedicalQueryBatchRow;
import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmission;
import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmissionItem;
import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmissionResult;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMedicalQueryRequestMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.mapper.BizQueryPriceMapper;
import com.ruoyi.business.service.IMedicalQueryBatchService;
import com.ruoyi.business.service.IMedicalQueryBatchSubmissionService;
import com.ruoyi.business.service.MedicalQueryException;

@Service
public class MedicalQueryBatchSubmissionServiceImpl implements IMedicalQueryBatchSubmissionService
{
    private static final String DELAYED_QUERY_TYPE = "precision_delayed";
    private static final BigDecimal ZERO_FEE = BigDecimal.ZERO.setScale(2);
    private static final DateTimeFormatter BATCH_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final BizInsuranceCompanyMapper companyMapper;
    private final BizQueryPriceMapper priceMapper;
    private final BizCompanyQueryPriceMapper companyPriceMapper;
    private final BizMonthlyUsageMapper monthlyUsageMapper;
    private final BizMedicalQueryRequestMapper requestMapper;
    private final BizMedicalQueryBatchMapper batchMapper;
    private final IMedicalQueryBatchService batchValidationService;
    private final TransactionTemplate transactionTemplate;

    public MedicalQueryBatchSubmissionServiceImpl(BizInsuranceCompanyMapper companyMapper,
            BizQueryPriceMapper priceMapper, BizCompanyQueryPriceMapper companyPriceMapper,
            BizMonthlyUsageMapper monthlyUsageMapper, BizMedicalQueryRequestMapper requestMapper,
            BizMedicalQueryBatchMapper batchMapper, IMedicalQueryBatchService batchValidationService,
            PlatformTransactionManager transactionManager)
    {
        this.companyMapper = companyMapper;
        this.priceMapper = priceMapper;
        this.companyPriceMapper = companyPriceMapper;
        this.monthlyUsageMapper = monthlyUsageMapper;
        this.requestMapper = requestMapper;
        this.batchMapper = batchMapper;
        this.batchValidationService = batchValidationService;
        this.transactionTemplate = transactionManager == null ? null : new TransactionTemplate(transactionManager);
    }

    @Override
    public MedicalQueryBatchSubmissionResult submit(Long companyId, MedicalQueryBatchSubmission command,
            String requestIp)
    {
        if (companyId == null || command == null || !"DELAYED".equalsIgnoreCase(command.getServiceMode()))
        {
            throw new MedicalQueryException("4000", "本阶段仅支持精准延时批次");
        }
        MedicalQueryBatchPreview preview = batchValidationService.validate(command.getRows());
        if (preview.getInvalidCount() > 0)
        {
            throw new MedicalQueryException("4006", "名单校验未通过");
        }
        assertUniqueRowNumbers(preview.getRows());
        return executeInTransaction(() -> submitInTransaction(companyId, preview.getRows(), requestIp));
    }

    private MedicalQueryBatchSubmissionResult submitInTransaction(Long companyId, List<MedicalQueryBatchRow> rows,
            String requestIp)
    {
        BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyByIdForUpdate(companyId);
        if (company == null || !"0".equals(company.getStatus()))
        {
            throw new MedicalQueryException("4002", "company disabled or not found");
        }
        String billingMonth = YearMonth.now().toString();
        boolean monthlyBudgetEnabled = isMonthlyBudgetEnabled(company);
        List<PendingItem> pendingItems = new ArrayList<>();
        int processingCount = 0;
        int newRequestCount = 0;

        for (MedicalQueryBatchRow row : rows)
        {
            BizMedicalQueryRequest existing = requestMapper.selectReusableDelayedRequest(companyId, row.getName(),
                    row.getIdCard());
            if (existing != null)
            {
                pendingItems.add(new PendingItem(row, existing, true));
                if ("PROCESSING".equals(existing.getProcessStatus()))
                {
                    processingCount++;
                }
                continue;
            }
            pendingItems.add(new PendingItem(row, null, false));
            newRequestCount++;
        }

        QueryPriceSnapshot price = newRequestCount == 0 ? null : resolvePrice(companyId);
        BigDecimal newReservation = price == null ? ZERO_FEE
                : price.hitFee.multiply(BigDecimal.valueOf(newRequestCount));
        if (!monthlyBudgetEnabled && nvl(company.getBalance()).compareTo(newReservation) < 0)
        {
            throw new MedicalQueryException("4001", "insufficient balance");
        }
        if (monthlyBudgetEnabled && newReservation.signum() > 0)
        {
            monthlyUsageMapper.ensureUsage(companyId, billingMonth, nvl(company.getMonthlyBudget()));
            requireUpdated(monthlyUsageMapper.reserveBudget(companyId, billingMonth, nvl(company.getMonthlyBudget()),
                    newReservation), "reserve batch query fee");
        }

        for (PendingItem pending : pendingItems)
        {
            if (pending.reused)
            {
                continue;
            }
            BizMedicalQueryRequest request = new BizMedicalQueryRequest();
            request.setRequestNo(generateRequestNo());
            request.setCompanyId(companyId);
            request.setEntryType("BATCH");
            request.setServiceMode("DELAYED");
            request.setPatientName(pending.row.getName());
            request.setIdCard(pending.row.getIdCard());
            request.setProcessStatus("PENDING");
            request.setUploadStatus("NOT_UPLOADED");
            request.setViewStatus("READ");
            request.setPriceConfigId(price.priceConfigId);
            request.setReservedFee(price.hitFee);
            request.setFeeSnapshot(ZERO_FEE);
            request.setBillingMonth(billingMonth);
            request.setRequestIp(requestIp);
            request.setVersion(0);
            requireUpdated(requestMapper.insertBizMedicalQueryRequest(request), "create batch request");
            pending.request = request;
        }

        int pendingCount = pendingItems.size() - processingCount;
        String batchStatus = pendingCount == 0 && processingCount > 0 ? "PROCESSING" : "PENDING";
        BizMedicalQueryBatch batch = new BizMedicalQueryBatch();
        batch.setBatchNo(generateBatchNo());
        batch.setCompanyId(companyId);
        batch.setServiceMode("DELAYED");
        batch.setBatchStatus(batchStatus);
        batch.setTotalCount(pendingItems.size());
        batch.setPendingCount(pendingCount);
        batch.setProcessingCount(processingCount);
        batch.setTotalFee(ZERO_FEE);
        batch.setRequestIp(requestIp);
        requireUpdated(batchMapper.insertBizMedicalQueryBatch(batch), "create medical query batch");
        if (batch.getId() == null)
        {
            throw new IllegalStateException("Batch ID was not generated");
        }

        MedicalQueryBatchSubmissionResult result = new MedicalQueryBatchSubmissionResult();
        result.setBatchNo(batch.getBatchNo());
        result.setBatchStatus(batchStatus);
        result.setTotalCount(pendingItems.size());
        result.setPendingCount(pendingCount);
        result.setProcessingCount(processingCount);
        result.setTotalFee(ZERO_FEE);
        for (PendingItem pending : pendingItems)
        {
            BizMedicalQueryBatchItem item = new BizMedicalQueryBatchItem();
            item.setBatchId(batch.getId());
            item.setRequestId(pending.request.getId());
            item.setRowNo(pending.row.getRowNo());
            item.setReusedFlag(pending.reused ? "1" : "0");
            item.setItemStatus("ACTIVE");
            requireUpdated(batchMapper.insertBizMedicalQueryBatchItem(item), "create medical query batch item");

            MedicalQueryBatchSubmissionItem submittedItem = new MedicalQueryBatchSubmissionItem();
            submittedItem.setRowNo(pending.row.getRowNo());
            submittedItem.setRequestNo(pending.request.getRequestNo());
            submittedItem.setReused(pending.reused);
            submittedItem.setProcessStatus(pending.request.getProcessStatus());
            submittedItem.setUploadStatus(pending.request.getUploadStatus());
            result.getItems().add(submittedItem);
            if (pending.reused)
            {
                result.setReusedCount(result.getReusedCount() + 1);
            }
        }
        return result;
    }

    private void assertUniqueRowNumbers(List<MedicalQueryBatchRow> rows)
    {
        Set<Integer> rowNumbers = new HashSet<>();
        for (MedicalQueryBatchRow row : rows)
        {
            if (row.getRowNo() == null || row.getRowNo() < 1 || !rowNumbers.add(row.getRowNo()))
            {
                throw new MedicalQueryException("4000", "名单行号必须为正数且不能重复");
            }
        }
    }

    private QueryPriceSnapshot resolvePrice(Long companyId)
    {
        BizQueryPrice basePrice = priceMapper.selectBizQueryPriceByQueryType(DELAYED_QUERY_TYPE);
        if (basePrice == null || !"0".equals(basePrice.getStatus()))
        {
            throw new MedicalQueryException("4003", "delayed query disabled or not found");
        }
        BizCompanyQueryPrice companyPrice = companyPriceMapper.selectCompanyPrice(companyId, DELAYED_QUERY_TYPE);
        if (companyPrice != null)
        {
            if (!"0".equals(companyPrice.getStatus()))
            {
                throw new MedicalQueryException("4003", "delayed query disabled or not found");
            }
            return new QueryPriceSnapshot(companyPrice.getId(), nvl(companyPrice.getHitFee()));
        }
        return new QueryPriceSnapshot(null, nvl(basePrice.getFee()));
    }

    private boolean isMonthlyBudgetEnabled(BizInsuranceCompany company)
    {
        return "0".equals(company.getBudgetEnabled()) && company.getMonthlyBudget() != null;
    }

    private String generateBatchNo()
    {
        return "B" + LocalDateTime.now().format(BATCH_TIME_FORMAT)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String generateRequestNo()
    {
        return "MB" + LocalDateTime.now().format(BATCH_TIME_FORMAT)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
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

    private static class PendingItem
    {
        private final MedicalQueryBatchRow row;
        private BizMedicalQueryRequest request;
        private final boolean reused;

        private PendingItem(MedicalQueryBatchRow row, BizMedicalQueryRequest request, boolean reused)
        {
            this.row = row;
            this.request = request;
            this.reused = reused;
        }
    }

    private static class QueryPriceSnapshot
    {
        private final Long priceConfigId;
        private final BigDecimal hitFee;

        private QueryPriceSnapshot(Long priceConfigId, BigDecimal hitFee)
        {
            this.priceConfigId = priceConfigId;
            this.hitFee = hitFee;
        }
    }
}
