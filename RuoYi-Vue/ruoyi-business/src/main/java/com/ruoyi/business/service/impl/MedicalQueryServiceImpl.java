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
        if (balanceBefore.compareTo(reserveAmount) < 0)
        {
            throw new MedicalQueryException("4001", "insufficient balance");
        }

        BizMedicalQueryRequest workflowRequest = buildWorkflowRequest(request, price, reserveAmount,
                YearMonth.now().toString());
        executeInTransaction(() -> {
            startRequest(company, workflowRequest);
            return null;
        });

        try
        {
            SourceResult sourceResult = querySource(request);
            Map<String, Object> data = DesensitizeUtil.desensitize(organizeRealtimeResult(sourceResult.data));
            String resultStatus = isNoResult(data) ? "NO_RESULT" : "HIT";
            BigDecimal actualFee = "HIT".equals(resultStatus) ? price.hitFee : ZERO_FEE;
            Completion completion = executeInTransaction(() -> completeSuccess(request, workflowRequest,
                    sourceResult.source, data, resultStatus, actualFee));
            return buildResult(workflowRequest, completion.log, resultStatus, actualFee, balanceBefore, data);
        }
        catch (RuntimeException e)
        {
            executeInTransaction(() -> {
                completeFailure(request, workflowRequest);
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

    private void startRequest(BizInsuranceCompany company, BizMedicalQueryRequest request)
    {
        requireUpdated(workflowRequestMapper.insertBizMedicalQueryRequest(request), "create realtime request");
        requireUpdated(companyMapper.deductBalance(company.getId(), request.getReservedFee()), "reserve balance");
        requireUpdated(workflowRequestMapper.markProcessing(request.getId()), "start realtime request");
    }

    private Completion completeSuccess(MedicalQueryRequest request, BizMedicalQueryRequest workflowRequest,
            String source, Map<String, Object> data, String resultStatus, BigDecimal actualFee)
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

        refundDifference(request.getCompanyId(), workflowRequest.getReservedFee(), actualFee);
        BizQueryLog log = insertQueryLog(request, workflowRequest, actualFee, resultStatus, "0", null);
        requireUpdated(workflowRequestMapper.finishRequest(workflowRequest.getId(), "COMPLETED", "UPLOADED",
                resultStatus, actualFee, log.getId()), "complete realtime request");
        return new Completion(log);
    }

    private void completeFailure(MedicalQueryRequest request, BizMedicalQueryRequest workflowRequest)
    {
        requireUpdated(companyMapper.addBalance(request.getCompanyId(), workflowRequest.getReservedFee()), "release balance");
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
        String patientName = value(request, "name");
        String idCard = value(request, "idCard");
        if (isEmpty(idCard))
        {
            idCard = value(request, "sfzhm");
        }
        workflow.setPatientName(isEmpty(patientName) ? "未提供" : patientName);
        workflow.setIdCard(idCard);
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
        result.setBalanceAfter(balanceBefore.subtract(nvl(actualFee)));
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

    private void refundDifference(Long companyId, BigDecimal reserved, BigDecimal actual)
    {
        BigDecimal refund = nvl(reserved).subtract(nvl(actual));
        if (refund.signum() > 0)
        {
            requireUpdated(companyMapper.addBalance(companyId, refund), "refund balance");
        }
    }

    private Map<String, Object> organizeRealtimeResult(Map<String, Object> sourceData)
    {
        if (sourceData == null || !(sourceData.get("res") instanceof Iterable<?> records))
        {
            return sourceData == null ? new LinkedHashMap<>() : sourceData;
        }
        Map<String, List<Map<String, Object>>> groupedRecords = new LinkedHashMap<>();
        int sourceIndex = 0;
        for (Object item : records)
        {
            if (!(item instanceof Map<?, ?> raw))
            {
                continue;
            }
            Map<String, Object> record = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : raw.entrySet())
            {
                if (entry.getKey() != null)
                {
                    record.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            String key = visitKey(record, sourceIndex++);
            groupedRecords.computeIfAbsent(key, ignored -> new ArrayList<>()).add(record);
        }
        List<List<Map<String, Object>>> orderedGroups = new ArrayList<>(groupedRecords.values());
        orderedGroups.sort(java.util.Comparator.comparing(group -> visitSortTime(group.get(0))));
        List<Map<String, Object>> visits = new ArrayList<>();
        int visitNo = 1;
        for (List<Map<String, Object>> group : orderedGroups)
        {
            Map<String, Object> record = group.get(0);
            String visitType = inpatientType(record);
            Map<String, Object> visit = new LinkedHashMap<>();
            visit.put("visitNo", visitNo++);
            visit.put("visitType", visitType);
            String[] basicFields = "住院".equals(visitType)
                    ? new String[] { "srno", "name", "sfzhm", "gender", "birth", "ryrq", "cyrq",
                    "jzlx", "hospitalname", "totalamount", "diseasecode", "diseasename" }
                    : new String[] { "srno", "name", "sfzhm", "gender", "birth", "jzsj", "jzlx",
                    "hospitalname", "totalamount", "diseasecode", "diseasename" };
            visit.put("basicInfo", tables(group, basicFields));
            if ("住院".equals(visitType))
            {
                visit.put("electronicMedicalRecord", tables(group, new String[] { "name", "sfzhm", "ryrq", "cyrq",
                        "ryzd", "cyzd", "ryqk", "zljg", "zs", "xbs", "jws" }));
                visit.put("medicalImaging", tables(group, new String[] { "name", "sfzhm", "hospitalname", "bgrq",
                        "ks", "bw", "yxbx", "yxzd" }));
            }
            visits.add(visit);
        }
        Map<String, Object> organized = new LinkedHashMap<>();
        organized.put("visits", visits);
        organized.put("totalVisits", visits.size());
        return organized;
    }

    private String visitKey(Map<String, Object> record, int sourceIndex)
    {
        String idCard = text(record.get("sfzhm"));
        String hospital = text(record.get("hospitalname"));
        if (hospital.isEmpty()) hospital = text(record.get("hospitalName"));
        String visitType = inpatientType(record);
        String start = "住院".equals(visitType) ? normalizeVisitTime(record.get("ryrq")) : normalizeVisitTime(record.get("jzsj"));
        String end = "住院".equals(visitType) ? normalizeVisitTime(record.get("cyrq")) : "";
        if (idCard.isEmpty() || hospital.isEmpty() || visitType.isEmpty() || start.isEmpty()
                || ("住院".equals(visitType) && end.isEmpty()))
        {
            return "UNMATCHED-" + sourceIndex;
        }
        return idCard + "\u0000" + hospital + "\u0000" + visitType + "\u0000" + start + "\u0000" + end;
    }

    private String normalizeVisitTime(Object value)
    {
        String time = text(value).replace('/', '-').replace('T', ' ');
        time = time.replaceAll("\\s+", " ").trim();
        if (!time.matches("\\d{4}-\\d{1,2}-\\d{1,2}( \\d{1,2}:\\d{1,2}(:\\d{1,2})?)?")) return time;
        String[] parts = time.split(" ", 2);
        String[] date = parts[0].split("-");
        String normalized = String.format("%s-%02d-%02d", date[0], Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        if (parts.length == 1) return normalized + " 00:00:00";
        String[] clock = parts[1].split(":");
        return normalized + String.format(" %02d:%02d:%02d", Integer.parseInt(clock[0]), Integer.parseInt(clock[1]), clock.length > 2 ? Integer.parseInt(clock[2]) : 0);
    }

    private String visitSortTime(Map<String, Object> record)
    {
        String type = inpatientType(record);
        return "住院".equals(type) ? normalizeVisitTime(record.get("ryrq")) : normalizeVisitTime(record.get("jzsj"));
    }

    private List<Map<String, Object>> tables(List<Map<String, Object>> records, String[] fields)
    {
        List<Map<String, Object>> values = new ArrayList<>();
        for (Map<String, Object> record : records)
        {
            Map<String, Object> row = table(record, fields);
            if (!values.contains(row)) values.add(row);
        }
        return values;
    }

    private Map<String, Object> table(Map<String, Object> record, String[] fields)
    {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String field : fields)
        {
            Object value = record.get(field);
            if ("totalamount".equals(field) && value == null)
            {
                value = record.get("totadiseasecodelamount");
            }
            values.put(fieldLabel(field), value == null ? "" : value);
        }
        return values;
    }

    private boolean hasValue(Map<String, Object> record, String... fields)
    {
        for (String field : fields)
        {
            if (!text(record.get(field)).isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    private String inpatientType(Map<String, Object> record)
    {
        String type = text(record.get("ryrq"));
        if ("住院".equals(type) || "门诊".equals(type))
        {
            return type;
        }
        type = text(record.get("jzlx"));
        if ("住院".equals(type) || "门诊".equals(type))
        {
            return type;
        }
        return type;
    }

    private String fieldLabel(String field)
    {
        return switch (field)
        {
            case "srno" -> "就诊id";
            case "sfzhm" -> "身份证号码";
            case "name" -> "姓名";
            case "gender" -> "性别";
            case "birth" -> "出生日期";
            case "ryrq" -> "入院日期";
            case "cyrq" -> "出院日期";
            case "jzsj" -> "就诊时间";
            case "jzlx" -> "就诊类型";
            case "hospitalname" -> "医院名称";
            case "totalamount" -> "总费用";
            case "diseasecode" -> "原始主诊断编码";
            case "diseasename" -> "原始主诊断名称";
            case "recordtype" -> "记录类型";
            case "ryzd" -> "入院诊断";
            case "cyzd" -> "出院诊断";
            case "ryqk" -> "入院情况";
            case "zljg" -> "诊疗经过";
            case "zs" -> "主诉";
            case "xbs" -> "现病史";
            case "jws" -> "既往史";
            case "yy" -> "医嘱";
            case "bgrq" -> "报告日期";
            case "ks" -> "科室";
            case "bw" -> "部位";
            case "yxbx" -> "影像表现";
            case "yxzd" -> "影像诊断";
            default -> field;
        };
    }

    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
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
        if (records == null)
        {
            records = data.get("res");
        }
        if (records == null)
        {
            records = data.get("visits");
        }
        return records instanceof Iterable<?> iterable && !iterable.iterator().hasNext();
    }

    private void validate(MedicalQueryRequest request)
    {
        if (request == null || request.getCompanyId() == null)
        {
            throw new MedicalQueryException("4000", "companyId is required");
        }
        if (isEmpty(request.getQueryType()))
        {
            throw new MedicalQueryException("4000", "queryType is required");
        }
        boolean newContract = !isEmpty(value(request, "sfzhm"))
                || !isEmpty(value(request, "startdate"))
                || !isEmpty(value(request, "enddate"));
        if (newContract)
        {
            if (isEmpty(value(request, "sfzhm")) || isEmpty(value(request, "startdate"))
                    || isEmpty(value(request, "enddate")))
            {
                throw new MedicalQueryException("4000", "sfzhm, startdate and enddate are required");
            }
            return;
        }
        if (isEmpty(value(request, "name")) || isEmpty(value(request, "idCard")))
        {
            throw new MedicalQueryException("4000", "name and idCard are required");
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
