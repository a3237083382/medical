package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizMedicalQueryResult;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.domain.medical.MedicalQueryResult;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMedicalQueryRequestMapper;
import com.ruoyi.business.mapper.BizMedicalQueryResultMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.mapper.BizQueryLogMapper;
import com.ruoyi.business.mapper.BizQueryPriceMapper;
import com.ruoyi.business.service.IBizHistoryQueryService;
import com.ruoyi.business.service.IMedicalQueryService;
import com.ruoyi.business.service.MedicalDataSource;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.business.util.DesensitizeUtil;

@Service
public class MedicalQueryServiceImpl implements IMedicalQueryService
{
    private static final BigDecimal ZERO_FEE = BigDecimal.ZERO.setScale(2);
    private static final DateTimeFormatter REQUEST_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final BizInsuranceCompanyMapper companyMapper;
    private final BizQueryPriceMapper priceMapper;
    private final BizQueryLogMapper queryLogMapper;
    private final MedicalDataSource medicalDataSource;
    private final BizCompanyQueryPriceMapper companyPriceMapper;
    private final BizMonthlyUsageMapper monthlyUsageMapper;
    private final IBizHistoryQueryService historyQueryService;
    private final BizMedicalQueryRequestMapper workflowRequestMapper;
    private final BizMedicalQueryResultMapper workflowResultMapper;
    private final TransactionTemplate transactionTemplate;

    public MedicalQueryServiceImpl(BizInsuranceCompanyMapper companyMapper, BizQueryPriceMapper priceMapper,
            BizQueryLogMapper queryLogMapper, MedicalDataSource medicalDataSource,
            BizCompanyQueryPriceMapper companyPriceMapper, BizMonthlyUsageMapper monthlyUsageMapper,
            IBizHistoryQueryService historyQueryService, BizMedicalQueryRequestMapper workflowRequestMapper,
            BizMedicalQueryResultMapper workflowResultMapper, PlatformTransactionManager transactionManager)
    {
        this.companyMapper = companyMapper;
        this.priceMapper = priceMapper;
        this.queryLogMapper = queryLogMapper;
        this.medicalDataSource = medicalDataSource;
        this.companyPriceMapper = companyPriceMapper;
        this.monthlyUsageMapper = monthlyUsageMapper;
        this.historyQueryService = historyQueryService;
        this.workflowRequestMapper = workflowRequestMapper;
        this.workflowResultMapper = workflowResultMapper;
        this.transactionTemplate = transactionManager == null ? null : new TransactionTemplate(transactionManager);
    }

    @Override
    public MedicalQueryResult query(MedicalQueryRequest request)
    {
        validate(request);
        BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyById(request.getCompanyId());
        if (company == null || !"0".equals(company.getStatus()))
        {
            throw new MedicalQueryException("4002", "company disabled or not found");
        }

        QueryPriceSnapshot price = resolveQueryPrice(request.getCompanyId(), request.getQueryType());
        BigDecimal reserveAmount = price.hitFee;
        BigDecimal balanceBefore = nvl(company.getBalance());
        boolean monthlyBudgetEnabled = isMonthlyBudgetEnabled(company);
        if (!monthlyBudgetEnabled && balanceBefore.compareTo(reserveAmount) < 0)
        {
            throw new MedicalQueryException("4001", "insufficient balance");
        }

        BizMedicalQueryRequest workflowRequest = buildWorkflowRequest(request, price, reserveAmount,
                YearMonth.now().toString());
        executeInTransaction(() -> {
            startRequest(company, workflowRequest, monthlyBudgetEnabled);
            return null;
        });

        try
        {
            SourceResult sourceResult = querySource(request);
            Map<String, Object> data = DesensitizeUtil.desensitize(sourceResult.data);
            String resultStatus = isNoResult(data) ? "NO_RESULT" : "HIT";
            BigDecimal actualFee = "HIT".equals(resultStatus) ? price.hitFee : ZERO_FEE;
            Completion completion = executeInTransaction(() -> completeSuccess(request, workflowRequest,
                    sourceResult.source, data, resultStatus, actualFee, monthlyBudgetEnabled));
            return buildResult(workflowRequest, completion.log, resultStatus, actualFee, balanceBefore, data);
        }
        catch (RuntimeException e)
        {
            executeInTransaction(() -> {
                completeFailure(request, workflowRequest, monthlyBudgetEnabled);
                return null;
            });
            if (e instanceof MedicalQueryException)
            {
                throw e;
            }
            throw new MedicalQueryException("5003", "medical data source request failed");
        }
    }

    public BigDecimal getBalance(Long companyId)
    {
        BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyById(companyId);
        if (company == null)
        {
            throw new MedicalQueryException("4002", "company not found");
        }
        return company.getBalance();
    }

    public BigDecimal getQueryPrice(String queryType)
    {
        BizQueryPrice price = priceMapper.selectBizQueryPriceByQueryType(queryType);
        if (price == null || !"0".equals(price.getStatus()))
        {
            throw new MedicalQueryException("4003", "query price disabled or not found");
        }
        return nvl(price.getFee());
    }

    private void startRequest(BizInsuranceCompany company, BizMedicalQueryRequest request, boolean monthlyBudgetEnabled)
    {
        requireUpdated(workflowRequestMapper.insertBizMedicalQueryRequest(request), "create realtime request");
        if (monthlyBudgetEnabled)
        {
            BigDecimal monthlyBudget = nvl(company.getMonthlyBudget());
            monthlyUsageMapper.ensureUsage(company.getId(), request.getBillingMonth(), monthlyBudget);
            int reserved = monthlyUsageMapper.reserveBudget(company.getId(), request.getBillingMonth(), monthlyBudget,
                    request.getReservedFee());
            if (reserved <= 0)
            {
                throw new MedicalQueryException("4001", "本月服务额度已达上限");
            }
        }
        requireUpdated(workflowRequestMapper.markProcessing(request.getId()), "start realtime request");
    }

    private Completion completeSuccess(MedicalQueryRequest request, BizMedicalQueryRequest workflowRequest,
            String source, Map<String, Object> data, String resultStatus, BigDecimal actualFee,
            boolean monthlyBudgetEnabled)
    {
        BizMedicalQueryResult storedResult = new BizMedicalQueryResult();
        storedResult.setRequestId(workflowRequest.getId());
        storedResult.setResultSource(source);
        storedResult.setColumnSchema(JSON.toJSONString(buildColumnSchema(data)));
        storedResult.setResultData(JSON.toJSONString(data));
        storedResult.setResultSummary("HIT".equals(resultStatus) ? "查询完成" : "未查得数据");
        storedResult.setVersion(1);
        storedResult.setUploadedBy("system");
        storedResult.setUploadedTime(new Date());
        requireUpdated(workflowResultMapper.insertBizMedicalQueryResult(storedResult), "save realtime result");

        if (monthlyBudgetEnabled)
        {
            requireUpdated(monthlyUsageMapper.confirmBudget(request.getCompanyId(), workflowRequest.getBillingMonth(),
                    workflowRequest.getReservedFee(), actualFee), "confirm realtime fee");
        }
        BizQueryLog log = insertQueryLog(request, workflowRequest, actualFee, resultStatus, "0", null);
        requireUpdated(workflowRequestMapper.finishRequest(workflowRequest.getId(), "COMPLETED", "UPLOADED",
                resultStatus, actualFee, log.getId()), "complete realtime request");
        return new Completion(log);
    }

    private void completeFailure(MedicalQueryRequest request, BizMedicalQueryRequest workflowRequest,
            boolean monthlyBudgetEnabled)
    {
        if (monthlyBudgetEnabled)
        {
            requireUpdated(monthlyUsageMapper.releaseBudget(request.getCompanyId(), workflowRequest.getBillingMonth(),
                    workflowRequest.getReservedFee()), "release realtime reservation");
        }
        BizQueryLog log = insertQueryLog(request, workflowRequest, ZERO_FEE, "FAILED", "1", "实时数据源查询失败");
        requireUpdated(workflowRequestMapper.finishRequest(workflowRequest.getId(), "FAILED", "NOT_UPLOADED",
                "FAILED", ZERO_FEE, log.getId()), "fail realtime request");
    }

    private BizQueryLog insertQueryLog(MedicalQueryRequest request, BizMedicalQueryRequest workflowRequest,
            BigDecimal fee, String resultStatus, String status, String remark)
    {
        BizQueryLog log = new BizQueryLog();
        log.setRequestNo(workflowRequest.getRequestNo());
        log.setServiceMode("REALTIME");
        log.setEntryType("SINGLE");
        log.setCompanyId(request.getCompanyId());
        log.setQueryType(request.getQueryType());
        log.setQueryParams(JSON.toJSONString(DesensitizeUtil.desensitize(request.getQueryParams())));
        log.setFee(fee);
        log.setBillingMonth(workflowRequest.getBillingMonth());
        log.setResultStatus(resultStatus);
        log.setFeeSnapshot(fee);
        log.setPriceConfigId(workflowRequest.getPriceConfigId());
        log.setStatus(status);
        log.setRequestIp(request.getRequestIp());
        log.setRequestTime(new Date());
        log.setRemark(remark);
        requireUpdated(queryLogMapper.insertBizQueryLog(log), "write realtime query log");
        return log;
    }

    private SourceResult querySource(MedicalQueryRequest request)
    {
        if ("history_medical".equals(request.getQueryType()))
        {
            Map<String, Object> data = historyQueryService.queryByPerson(value(request, "name"), value(request, "idCard"));
            return new SourceResult("HISTORY", data == null ? new LinkedHashMap<>() : data);
        }
        String source = normalizeSource(medicalDataSource.sourceCode(request));
        Map<String, Object> sourceData = medicalDataSource.query(request);
        if (historyQueryService != null && sourceData != null && !sourceData.isEmpty())
        {
            historyQueryService.cacheQueryResult(request.getQueryType(), sourceData);
        }
        return new SourceResult(source, sourceData == null ? new LinkedHashMap<>() : sourceData);
    }

    private QueryPriceSnapshot resolveQueryPrice(Long companyId, String queryType)
    {
        BizCompanyQueryPrice companyPrice = companyPriceMapper.selectCompanyPrice(companyId, queryType);
        if (companyPrice != null)
        {
            if (!"0".equals(companyPrice.getStatus()))
            {
                throw new MedicalQueryException("4003", "query price disabled or not found");
            }
            return new QueryPriceSnapshot(companyPrice.getId(), nvl(companyPrice.getHitFee()));
        }
        return new QueryPriceSnapshot(null, getQueryPrice(queryType));
    }

    private BizMedicalQueryRequest buildWorkflowRequest(MedicalQueryRequest request, QueryPriceSnapshot price,
            BigDecimal reserveAmount, String billingMonth)
    {
        BizMedicalQueryRequest workflow = new BizMedicalQueryRequest();
        workflow.setRequestNo(generateRequestNo());
        workflow.setCompanyId(request.getCompanyId());
        workflow.setEntryType("SINGLE");
        workflow.setServiceMode("REALTIME");
        workflow.setQueryType(request.getQueryType());
        workflow.setPatientName(value(request, "name"));
        workflow.setIdCard(value(request, "idCard"));
        workflow.setProcessStatus("PENDING");
        workflow.setUploadStatus("NOT_UPLOADED");
        workflow.setViewStatus("READ");
        workflow.setPriceConfigId(price.priceConfigId);
        workflow.setReservedFee(reserveAmount);
        workflow.setFeeSnapshot(ZERO_FEE);
        workflow.setBillingMonth(billingMonth);
        workflow.setRequestIp(request.getRequestIp());
        workflow.setVersion(0);
        return workflow;
    }

    private MedicalQueryResult buildResult(BizMedicalQueryRequest workflow, BizQueryLog log, String resultStatus,
            BigDecimal actualFee, BigDecimal balanceBefore, Map<String, Object> data)
    {
        MedicalQueryResult result = new MedicalQueryResult();
        result.setQueryId(log.getId());
        result.setRequestNo(workflow.getRequestNo());
        result.setProcessStatus("COMPLETED");
        result.setUploadStatus("UPLOADED");
        result.setResultStatus(resultStatus);
        result.setServiceStatus("NORMAL");
        result.setFee(actualFee);
        result.setBalanceAfter(balanceBefore);
        result.setData(data);
        return result;
    }

    private List<Map<String, Object>> buildColumnSchema(Map<String, Object> data)
    {
        List<Map<String, Object>> schema = new ArrayList<>();
        int order = 0;
        for (String field : data.keySet())
        {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("field", field);
            column.put("label", field);
            column.put("order", order++);
            schema.add(column);
        }
        return schema;
    }

    private boolean isMonthlyBudgetEnabled(BizInsuranceCompany company)
    {
        return "0".equals(company.getBudgetEnabled()) && company.getMonthlyBudget() != null;
    }

    private boolean isNoResult(Map<String, Object> data)
    {
        if (data == null || data.isEmpty())
        {
            return true;
        }
        Object records = data.get("records");
        return records instanceof Iterable<?> iterable && !iterable.iterator().hasNext();
    }

    private void validate(MedicalQueryRequest request)
    {
        if (request == null || request.getCompanyId() == null)
        {
            throw new MedicalQueryException("4000", "companyId is required");
        }
        if (isEmpty(request.getQueryType()) || isEmpty(value(request, "name")) || isEmpty(value(request, "idCard")))
        {
            throw new MedicalQueryException("4000", "queryType, name and idCard are required");
        }
    }

    private String value(MedicalQueryRequest request, String key)
    {
        Object value = request.getQueryParams() == null ? null : request.getQueryParams().get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private boolean isEmpty(String value)
    {
        return value == null || value.isEmpty();
    }

    private String generateRequestNo()
    {
        return "MR" + LocalDateTime.now().format(REQUEST_TIME_FORMAT)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String normalizeSource(String source)
    {
        if ("digital".equalsIgnoreCase(source))
        {
            return "DIGITAL_INDUSTRY";
        }
        if ("history".equalsIgnoreCase(source))
        {
            return "HISTORY";
        }
        return "MOCK";
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

    private static class SourceResult
    {
        private final String source;
        private final Map<String, Object> data;

        private SourceResult(String source, Map<String, Object> data)
        {
            this.source = source;
            this.data = data;
        }
    }

    private static class Completion
    {
        private final BizQueryLog log;

        private Completion(BizQueryLog log)
        {
            this.log = log;
        }
    }
}
