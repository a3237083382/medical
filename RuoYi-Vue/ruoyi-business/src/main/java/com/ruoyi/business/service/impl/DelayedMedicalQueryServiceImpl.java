package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizMedicalQueryResult;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.DelayedMedicalQuerySubmission;
import com.ruoyi.business.domain.medical.MedicalQueryRequestDetail;
import com.ruoyi.business.domain.medical.MedicalQueryBatchProgress;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMedicalQueryRequestMapper;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMedicalQueryResultMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.mapper.BizQueryPriceMapper;
import com.ruoyi.business.service.IDelayedMedicalQueryService;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.business.util.DesensitizeUtil;

@Service
public class DelayedMedicalQueryServiceImpl implements IDelayedMedicalQueryService
{
    private static final String DELAYED_QUERY_TYPE = "precision_delayed";
    private static final BigDecimal ZERO_FEE = BigDecimal.ZERO.setScale(2);
    private static final DateTimeFormatter REQUEST_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[0-9]{17}[0-9Xx]$");

    private final BizInsuranceCompanyMapper companyMapper;
    private final BizQueryPriceMapper priceMapper;
    private final BizCompanyQueryPriceMapper companyPriceMapper;
    private final BizMonthlyUsageMapper monthlyUsageMapper;
    private final BizMedicalQueryRequestMapper requestMapper;
    private final BizMedicalQueryBatchMapper batchMapper;
    private final BizMedicalQueryResultMapper resultMapper;
    private final TransactionTemplate transactionTemplate;

    public DelayedMedicalQueryServiceImpl(BizInsuranceCompanyMapper companyMapper, BizQueryPriceMapper priceMapper,
            BizCompanyQueryPriceMapper companyPriceMapper, BizMonthlyUsageMapper monthlyUsageMapper,
            BizMedicalQueryRequestMapper requestMapper, BizMedicalQueryBatchMapper batchMapper,
            BizMedicalQueryResultMapper resultMapper,
            PlatformTransactionManager transactionManager)
    {
        this.companyMapper = companyMapper;
        this.priceMapper = priceMapper;
        this.companyPriceMapper = companyPriceMapper;
        this.monthlyUsageMapper = monthlyUsageMapper;
        this.requestMapper = requestMapper;
        this.batchMapper = batchMapper;
        this.resultMapper = resultMapper;
        this.transactionTemplate = transactionManager == null ? null : new TransactionTemplate(transactionManager);
    }

    @Override
    public DelayedMedicalQuerySubmission submit(Long companyId, String patientName, String idCard, String requestIp)
    {
        String normalizedName = normalize(patientName);
        String normalizedIdCard = normalize(idCard).toUpperCase();
        validateSubmission(companyId, normalizedName, normalizedIdCard);
        return executeInTransaction(() -> submitInTransaction(companyId, normalizedName, normalizedIdCard, requestIp));
    }

    @Override
    public MedicalQueryRequestDetail getRequest(Long companyId, String requestNo)
    {
        if (companyId == null || isEmpty(normalize(requestNo)))
        {
            throw new MedicalQueryException("4000", "companyId and requestNo are required");
        }
        BizMedicalQueryRequest request = requestMapper.selectCompanyRequestByNo(companyId, normalize(requestNo));
        if (request == null || !"DELAYED".equals(request.getServiceMode()))
        {
            throw new MedicalQueryException("4041", "request not found");
        }
        return buildDetail(request);
    }

    @Override
    public List<MedicalQueryRequestDetail> listHistory(Long companyId, String requestNo, String patientName,
            String processStatus, String resultStatus, String beginTime, String endTime)
    {
        if (companyId == null)
        {
            throw new MedicalQueryException("4000", "companyId is required");
        }
        BizMedicalQueryRequest query = new BizMedicalQueryRequest();
        query.setCompanyId(companyId);
        query.setRequestNo(trim(requestNo));
        query.setPatientName(trim(patientName));
        query.setProcessStatus(trim(processStatus));
        query.setResultStatus(trim(resultStatus));
        query.getParams().put("beginTime", trim(beginTime));
        query.getParams().put("endTime", trim(endTime));
        List<MedicalQueryRequestDetail> history = new ArrayList<>();
        for (BizMedicalQueryRequest request : requestMapper.selectCompanyDelayedHistory(query))
        {
            history.add(buildSummary(request));
        }
        return history;
    }

    @Override
    public List<MedicalQueryBatchProgress> listBatchHistory(Long companyId)
    {
        if (companyId == null)
        {
            throw new MedicalQueryException("4000", "companyId is required");
        }
        return batchMapper.selectCompanyBatchHistory(companyId);
    }

    @Override
    public int countUnread(Long companyId)
    {
        if (companyId == null)
        {
            throw new MedicalQueryException("4000", "companyId is required");
        }
        return requestMapper.countCompanyUnreadDelayedResults(companyId);
    }

    @Override
    public void markRead(Long companyId, String requestNo)
    {
        if (companyId == null || isEmpty(normalize(requestNo)))
        {
            throw new MedicalQueryException("4000", "companyId and requestNo are required");
        }
        BizMedicalQueryRequest request = requestMapper.selectCompanyRequestByNo(companyId, normalize(requestNo));
        if (request == null || !"DELAYED".equals(request.getServiceMode()))
        {
            throw new MedicalQueryException("4041", "request not found");
        }
        if (!"COMPLETED".equals(request.getProcessStatus()) || !"UPLOADED".equals(request.getUploadStatus()))
        {
            throw new MedicalQueryException("4093", "result is not ready");
        }
        if ("UNREAD".equals(request.getViewStatus()))
        {
            requestMapper.markCompanyDelayedRequestRead(companyId, normalize(requestNo));
        }
    }

    private DelayedMedicalQuerySubmission submitInTransaction(Long companyId, String patientName, String idCard,
            String requestIp)
    {
        BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyByIdForUpdate(companyId);
        if (company == null || !"0".equals(company.getStatus()))
        {
            throw new MedicalQueryException("4002", "company disabled or not found");
        }

        BizMedicalQueryRequest existing = requestMapper.selectReusableDelayedRequest(companyId, patientName, idCard);
        if (existing != null)
        {
            return buildSubmission(existing, true);
        }

        QueryPriceSnapshot price = resolvePrice(companyId);
        boolean monthlyBudgetEnabled = isMonthlyBudgetEnabled(company);
        if (!monthlyBudgetEnabled && nvl(company.getBalance()).compareTo(price.hitFee) < 0)
        {
            throw new MedicalQueryException("4001", "insufficient balance");
        }

        BizMedicalQueryRequest request = new BizMedicalQueryRequest();
        request.setRequestNo(generateRequestNo());
        request.setCompanyId(companyId);
        request.setEntryType("SINGLE");
        request.setServiceMode("DELAYED");
        request.setPatientName(patientName);
        request.setIdCard(idCard);
        request.setProcessStatus("PENDING");
        request.setUploadStatus("NOT_UPLOADED");
        request.setViewStatus("READ");
        request.setPriceConfigId(price.priceConfigId);
        request.setReservedFee(price.hitFee);
        request.setFeeSnapshot(ZERO_FEE);
        request.setBillingMonth(YearMonth.now().toString());
        request.setRequestIp(requestIp);
        request.setVersion(0);
        request.setCreateTime(new Date());
        requireUpdated(requestMapper.insertBizMedicalQueryRequest(request), "create delayed request");

        if (monthlyBudgetEnabled)
        {
            BigDecimal monthlyBudget = nvl(company.getMonthlyBudget());
            monthlyUsageMapper.ensureUsage(companyId, request.getBillingMonth(), monthlyBudget);
            requireUpdated(monthlyUsageMapper.reserveBudget(companyId, request.getBillingMonth(), monthlyBudget,
                    request.getReservedFee()), "reserve delayed query fee");
        }
        return buildSubmission(request, false);
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

    private MedicalQueryRequestDetail buildDetail(BizMedicalQueryRequest request)
    {
        MedicalQueryRequestDetail detail = buildSummary(request);
        if ("UPLOADED".equals(request.getUploadStatus()))
        {
            BizMedicalQueryResult storedResult = resultMapper.selectByRequestId(request.getId());
            if (storedResult != null)
            {
                detail.setResultVisible(true);
                detail.setColumnSchema(parseJson(storedResult.getColumnSchema()));
                detail.setData(DesensitizeUtil.desensitize(parseData(storedResult.getResultData())));
                detail.setResultSummary(storedResult.getResultSummary());
            }
        }
        return detail;
    }

    private MedicalQueryRequestDetail buildSummary(BizMedicalQueryRequest request)
    {
        Map<String, Object> identity = new LinkedHashMap<>();
        identity.put("patientName", request.getPatientName());
        identity.put("idCard", request.getIdCard());
        Map<String, Object> maskedIdentity = DesensitizeUtil.desensitize(identity);

        MedicalQueryRequestDetail detail = new MedicalQueryRequestDetail();
        detail.setRequestNo(request.getRequestNo());
        detail.setEntryType(request.getEntryType());
        detail.setServiceMode(request.getServiceMode());
        detail.setQueryType(request.getQueryType());
        detail.setPatientName((String) maskedIdentity.get("patientName"));
        detail.setIdCard((String) maskedIdentity.get("idCard"));
        detail.setProcessStatus(request.getProcessStatus());
        detail.setUploadStatus(request.getUploadStatus());
        detail.setResultStatus(request.getResultStatus());
        detail.setViewStatus(request.getViewStatus());
        detail.setFee(nvl(request.getFeeSnapshot()));
        detail.setSubmittedTime(request.getCreateTime());
        detail.setProcessStartTime(request.getProcessStartTime());
        detail.setCompleteTime(request.getCompleteTime());

        return detail;
    }

    private DelayedMedicalQuerySubmission buildSubmission(BizMedicalQueryRequest request, boolean reused)
    {
        DelayedMedicalQuerySubmission submission = new DelayedMedicalQuerySubmission();
        submission.setRequestNo(request.getRequestNo());
        submission.setProcessStatus(request.getProcessStatus());
        submission.setUploadStatus(request.getUploadStatus());
        submission.setResultStatus(request.getResultStatus());
        submission.setReused(reused);
        submission.setSubmittedTime(request.getCreateTime());
        return submission;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseData(String json)
    {
        if (isEmpty(json))
        {
            return new LinkedHashMap<>();
        }
        return JSON.parseObject(json, LinkedHashMap.class);
    }

    private Object parseJson(String json)
    {
        return isEmpty(json) ? null : JSON.parse(json);
    }

    private void validateSubmission(Long companyId, String patientName, String idCard)
    {
        if (companyId == null || isEmpty(patientName) || isEmpty(idCard))
        {
            throw new MedicalQueryException("4000", "companyId, name and idCard are required");
        }
        if (patientName.length() > 50 || !ID_CARD_PATTERN.matcher(idCard).matches())
        {
            throw new MedicalQueryException("4000", "name or idCard format is invalid");
        }
    }

    private boolean isMonthlyBudgetEnabled(BizInsuranceCompany company)
    {
        return "0".equals(company.getBudgetEnabled()) && company.getMonthlyBudget() != null;
    }

    private String generateRequestNo()
    {
        return "MD" + LocalDateTime.now().format(REQUEST_TIME_FORMAT)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String normalize(String value)
    {
        return value == null ? "" : value.trim();
    }

    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    private boolean isEmpty(String value)
    {
        return value == null || value.isEmpty();
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
