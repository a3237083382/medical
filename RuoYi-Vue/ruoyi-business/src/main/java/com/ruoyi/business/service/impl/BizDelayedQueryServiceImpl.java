package com.ruoyi.business.service.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizDelayedQueryImport;
import com.ruoyi.business.domain.BizDelayedQueryRequest;
import com.ruoyi.business.domain.BizDelayedQueryResult;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizDelayedQueryImportMapper;
import com.ruoyi.business.mapper.BizDelayedQueryRequestMapper;
import com.ruoyi.business.mapper.BizDelayedQueryResultMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.mapper.BizQueryLogMapper;
import com.ruoyi.business.service.IBizDelayedQueryService;
import com.ruoyi.business.service.MedicalQueryException;

@Service
public class BizDelayedQueryServiceImpl implements IBizDelayedQueryService
{
    private static final int MAX_BATCH_SIZE = 500;
    private static final BigDecimal ZERO_FEE = new BigDecimal("0.00");
    private static final Pattern DATE_PREFIX = Pattern.compile("^(\\d{4})[-/.年](\\d{1,2})[-/.月](\\d{1,2})");

    public static final String BILLING_QUERY_TYPE = "delayed_precise";
    public static final String QUERY_TYPE_MEDICAL = "MEDICAL";
    public static final String QUERY_TYPE_BIG_DATA = "BIG_DATA";
    public static final String QUERY_PENDING = "PENDING";
    public static final String QUERY_QUERIED = "QUERIED";
    public static final String UPLOAD_NOT_UPLOADED = "NOT_UPLOADED";
    public static final String UPLOAD_UPLOADED = "UPLOADED";
    public static final String RESULT_HIT = "HIT";
    public static final String RESULT_NO_RESULT = "NO_RESULT";
    public static final String RESULT_PARTIAL = "PARTIAL";
    private static final String COVERAGE_RECORD_MARKER = "\"__recordType\":\"INSURANCE_COVERAGE\"";

    private final BizDelayedQueryRequestMapper requestMapper;
    private final BizDelayedQueryResultMapper resultMapper;
    private final BizInsuranceCompanyMapper companyMapper;
    private final BizCompanyQueryPriceMapper priceMapper;
    private final BizMonthlyUsageMapper monthlyUsageMapper;
    private final BizQueryLogMapper queryLogMapper;
    private final BizDelayedQueryImportMapper importMapper;

    @Autowired
    public BizDelayedQueryServiceImpl(BizDelayedQueryRequestMapper requestMapper,
            BizDelayedQueryResultMapper resultMapper, BizInsuranceCompanyMapper companyMapper,
            BizCompanyQueryPriceMapper priceMapper, BizMonthlyUsageMapper monthlyUsageMapper,
            BizQueryLogMapper queryLogMapper, BizDelayedQueryImportMapper importMapper)
    {
        this.requestMapper = requestMapper;
        this.resultMapper = resultMapper;
        this.companyMapper = companyMapper;
        this.priceMapper = priceMapper;
        this.monthlyUsageMapper = monthlyUsageMapper;
        this.queryLogMapper = queryLogMapper;
        this.importMapper = importMapper;
    }

    public BizDelayedQueryServiceImpl(BizDelayedQueryRequestMapper requestMapper,
            BizDelayedQueryResultMapper resultMapper, BizInsuranceCompanyMapper companyMapper,
            BizCompanyQueryPriceMapper priceMapper, BizMonthlyUsageMapper monthlyUsageMapper,
            BizQueryLogMapper queryLogMapper)
    {
        this(requestMapper, resultMapper, companyMapper, priceMapper, monthlyUsageMapper, queryLogMapper, null);
    }

    @Override
    @Transactional
    public BizDelayedQueryRequest submit(Long companyId, String companyName, String patientName, String idCard, String requestIp)
    {
        return submit(companyId, companyName, patientName, idCard, QUERY_TYPE_MEDICAL, requestIp);
    }

    @Override
    @Transactional
    public BizDelayedQueryRequest submit(Long companyId, String companyName, String patientName, String idCard,
            String queryType, String requestIp)
    {
        return submit(companyId, companyName, patientName, idCard, normalizeQueryType(queryType), requestIp, null);
    }

    private BizDelayedQueryRequest submit(Long companyId, String companyName, String patientName, String idCard,
            String queryType, String requestIp, String batchNo)
    {
        if (empty(patientName) || empty(idCard))
        {
            throw new IllegalArgumentException("patientName and idCard are required");
        }
        BizDelayedQueryRequest duplicate = requestMapper.selectPendingDuplicate(companyId, patientName, idCard, queryType);
        if (duplicate != null)
        {
            duplicate.setResults(new ArrayList<>());
            return duplicate;
        }
        BizDelayedQueryRequest request = new BizDelayedQueryRequest();
        Billing reservation = calculateBilling(companyId, RESULT_HIT);
        reserveMonthlyUsage(companyId, reservation.billingMonth, reservation.reservedFee);
        request.setRequestNo(newRequestNo());
        request.setBatchNo(batchNo);
        request.setCompanyId(companyId);
        request.setCompanyNameSnapshot(companyName);
        request.setPatientName(patientName);
        request.setIdCard(idCard);
        request.setQueryType(queryType);
        request.setQueryStatus(QUERY_PENDING);
        request.setUploadStatus(UPLOAD_NOT_UPLOADED);
        request.setChargedFlag("0");
        request.setRequestIp(requestIp);
        request.setReservedFee(reservation.reservedFee);
        request.setBillingMonth(reservation.billingMonth);
        request.setPriceConfigId(reservation.priceConfigId);
        requestMapper.insertBizDelayedQueryRequest(request);
        return request;
    }

    @Override
    @Transactional
    public List<BizDelayedQueryRequest> submitBatch(Long companyId, String companyName,
            List<BizDelayedQueryRequest> requests, String requestIp)
    {
        return submitBatch(companyId, companyName, requests, QUERY_TYPE_MEDICAL, requestIp);
    }

    @Override
    @Transactional
    public List<BizDelayedQueryRequest> submitBatch(Long companyId, String companyName,
            List<BizDelayedQueryRequest> requests, String queryType, String requestIp)
    {
        if (requests == null || requests.isEmpty())
        {
            throw new IllegalArgumentException("items are required");
        }
        if (requests.size() > MAX_BATCH_SIZE)
        {
            throw new IllegalArgumentException("名单最多支持500人");
        }
        List<BizDelayedQueryRequest> submitted = new ArrayList<>();
        String batchNo = "BD" + UUID.randomUUID().toString().replace("-", "");
        String normalizedQueryType = normalizeQueryType(queryType);
        for (BizDelayedQueryRequest item : requests)
        {
            if (item == null)
            {
                throw new IllegalArgumentException("patientName and idCard are required");
            }
            submitted.add(submit(companyId, companyName, item.getPatientName(), item.getIdCard(),
                    normalizedQueryType, requestIp, batchNo));
        }
        return submitted;
    }

    @Override
    public List<BizDelayedQueryRequest> selectList(BizDelayedQueryRequest request)
    {
        return requestMapper.selectBizDelayedQueryRequestList(request);
    }

    @Override
    public int countPendingRequests()
    {
        return requestMapper.countPendingRequests();
    }

    @Override
    @Transactional
    public Map<String, Object> cancelBatch(Long companyId, String batchNo)
    {
        BizDelayedQueryRequest filter = new BizDelayedQueryRequest();
        filter.setCompanyId(companyId);
        filter.setBatchNo(batchNo);
        List<BizDelayedQueryRequest> requests = requestMapper.selectBizDelayedQueryRequestList(filter);
        int cancelled = 0;
        int notCancellable = 0;
        for (BizDelayedQueryRequest request : requests)
        {
            if (cancelIfPending(companyId, request)) cancelled++;
            else notCancellable++;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batchNo", batchNo);
        result.put("cancelledCount", cancelled);
        result.put("notCancellableCount", notCancellable);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> cancelItem(Long companyId, Long id)
    {
        BizDelayedQueryRequest request = requestMapper.selectBizDelayedQueryRequestById(id);
        if (request == null || !companyId.equals(request.getCompanyId()))
        {
            throw new IllegalArgumentException("请求不存在");
        }
        if (!cancelIfPending(companyId, request))
        {
            throw new IllegalStateException("当前状态不可取消");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("cancelled", true);
        return result;
    }

    private boolean cancelIfPending(Long companyId, BizDelayedQueryRequest request)
    {
        if (!QUERY_PENDING.equals(request.getQueryStatus()) || !UPLOAD_NOT_UPLOADED.equals(request.getUploadStatus()))
        {
            return false;
        }
        BigDecimal reserved = nvl(request.getReservedFee());
        String billingMonth = request.getBillingMonth();
        if (requestMapper.cancelPendingRequest(request.getId(), companyId) != 1)
        {
            return false;
        }
        resultMapper.deleteByRequestId(request.getId());
        if (monthlyUsageMapper != null && reserved.signum() > 0 && billingMonth != null)
        {
            monthlyUsageMapper.releaseBudget(companyId, billingMonth, reserved);
        }
        return true;
    }

    @Override
    public BizDelayedQueryRequest selectAdminDetail(Long id)
    {
        BizDelayedQueryRequest request = requireRequest(id);
        request.setResults(resultMapper.selectByRequestId(id));
        return request;
    }

    @Override
    public BizDelayedQueryRequest selectCompanyDetail(Long id, Long companyId)
    {
        BizDelayedQueryRequest request = requireRequest(id);
        if (!companyId.equals(request.getCompanyId()))
        {
            return null;
        }
        if (UPLOAD_UPLOADED.equals(request.getUploadStatus()))
        {
            request.setResults(resultMapper.selectByRequestId(id));
        }
        else
        {
            request.setResults(new ArrayList<>());
        }
        return request;
    }

    @Override
    @Transactional
    public BizDelayedQueryRequest saveDraft(Long id, List<BizDelayedQueryResult> results,
            String resultStatus, String resultMessage, String handlerName)
    {
        BizDelayedQueryRequest current = requireRequest(id);
        ensureNotCancelled(current);
        replaceResults(id, results, handlerName);
        BizDelayedQueryRequest update = new BizDelayedQueryRequest();
        update.setId(id);
        update.setQueryStatus(empty(resultStatus) ? current.getQueryStatus() : QUERY_PENDING);
        update.setUploadStatus(UPLOAD_NOT_UPLOADED);
        update.setResultStatus(resultStatus);
        update.setResultMessage(resultMessage);
        update.setHandlerName(handlerName);
        update.setUpdateBy(handlerName);
        requestMapper.updateBizDelayedQueryRequest(update);
        return selectAdminDetail(id);
    }

    @Override
    @Transactional
    public BizDelayedQueryRequest complete(Long id, List<BizDelayedQueryResult> results,
            String resultStatus, String resultMessage, String handlerName)
    {
        BizDelayedQueryRequest current = requireRequest(id);
        ensureNotCancelled(current);
        String finalResultStatus = normalizeResultStatus(resultStatus);
        validateFinalResult(results, finalResultStatus, resultMessage);
        replaceResults(id, results, handlerName);
        Date now = new Date();
        String billingMonth = empty(current.getBillingMonth()) ? currentBillingMonth() : current.getBillingMonth();
        Billing billing = calculateBilling(current.getCompanyId(), finalResultStatus, billingMonth);
        BigDecimal reservedFee = nvl(current.getReservedFee());
        if (!"1".equals(current.getChargedFlag()))
        {
            confirmMonthlyUsage(current.getCompanyId(), billing.billingMonth, reservedFee, billing.fee);
            insertBillingLog(current, finalResultStatus, billing);
        }

        BizDelayedQueryRequest update = new BizDelayedQueryRequest();
        update.setId(id);
        update.setQueryStatus(QUERY_QUERIED);
        update.setUploadStatus(UPLOAD_UPLOADED);
        update.setResultStatus(finalResultStatus);
        update.setResultMessage(resultMessage);
        update.setHandlerName(handlerName);
        update.setHandledTime(now);
        update.setUploadedTime(now);
        update.setFee(billing.fee);
        update.setBillingMonth(billing.billingMonth);
        update.setChargedFlag("1");
        update.setPriceConfigId(billing.priceConfigId);
        update.setUpdateBy(handlerName);
        requestMapper.updateBizDelayedQueryRequest(update);
        return selectAdminDetail(id);
    }

    @Override
    @Transactional
    public BizDelayedQueryRequest updateUploadedResult(Long id, List<BizDelayedQueryResult> results,
            String resultStatus, String resultMessage, String modifyBy, String modifyReason)
    {
        BizDelayedQueryRequest current = requireRequest(id);
        if (!UPLOAD_UPLOADED.equals(current.getUploadStatus()))
        {
            throw new IllegalStateException("Only uploaded requests can be modified");
        }
        String finalResultStatus = normalizeResultStatus(resultStatus);
        validateFinalResult(results, finalResultStatus, resultMessage);
        replaceResults(id, results, modifyBy);
        BizDelayedQueryRequest update = new BizDelayedQueryRequest();
        update.setId(id);
        update.setQueryStatus(QUERY_QUERIED);
        update.setUploadStatus(UPLOAD_UPLOADED);
        update.setResultStatus(finalResultStatus);
        update.setResultMessage(resultMessage);
        update.setModifyBy(modifyBy);
        update.setModifyTime(new Date());
        update.setModifyReason(modifyReason);
        update.setUpdateBy(modifyBy);
        requestMapper.updateBizDelayedQueryRequest(update);
        return selectAdminDetail(id);
    }

    @Override
    @Transactional
    public BizDelayedQueryRequest importExcel(Long id, MultipartFile file, String operator) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("file is required");
        }
        BizDelayedQueryRequest request = requireRequest(id);
        ensureNotCancelled(request);
        List<BizDelayedQueryResult> results = parseExcel(file, operator, request.getQueryType());
        for (BizDelayedQueryResult existing : resultMapper.selectByRequestId(id))
        {
            if (isCoverageResult(existing))
            {
                results.add(existing);
            }
        }
        if (importMapper != null)
        {
            BizDelayedQueryImport importLog = new BizDelayedQueryImport();
            importLog.setRequestId(id);
            importLog.setFileName(file.getOriginalFilename());
            importLog.setFileSize(file.getSize());
            importLog.setTotalRows(results.size());
            importLog.setSuccessRows(results.size());
            importLog.setFailedRows(0);
            importLog.setStatus("1");
            importLog.setCreateBy(operator);
            importMapper.insertBizDelayedQueryImport(importLog);
        }
        return saveDraft(id, results, RESULT_HIT, null, operator);
    }

    @Override
    public CompanyLogs selectCompanyLogs(Long companyId)
    {
        BizQueryLog queryLog = new BizQueryLog();
        queryLog.setCompanyId(companyId);
        BizDelayedQueryRequest delayed = new BizDelayedQueryRequest();
        delayed.setCompanyId(companyId);
        return new CompanyLogs(queryLogMapper.selectBizQueryLogList(queryLog),
                requestMapper.selectBizDelayedQueryRequestList(delayed));
    }

    private BizDelayedQueryRequest requireRequest(Long id)
    {
        BizDelayedQueryRequest request = requestMapper.selectBizDelayedQueryRequestById(id);
        if (request == null)
        {
            throw new IllegalArgumentException("request not found");
        }
        return request;
    }

    private void ensureNotCancelled(BizDelayedQueryRequest request)
    {
        if ("CANCELLED".equals(request.getQueryStatus()))
        {
            throw new IllegalStateException("Cancelled requests cannot be processed");
        }
    }

    private void replaceResults(Long requestId, List<BizDelayedQueryResult> results, String operator)
    {
        resultMapper.deleteByRequestId(requestId);
        if (results == null)
        {
            return;
        }
        int rowNo = 1;
        for (BizDelayedQueryResult result : results)
        {
            if (result == null || empty(result.getRawJson()))
            {
                continue;
            }
            result.setRequestId(requestId);
            result.setRowNo(rowNo++);
            result.setCreateBy(operator);
            resultMapper.insertBizDelayedQueryResult(result);
        }
    }

    private String normalizeResultStatus(String resultStatus)
    {
        return empty(resultStatus) ? RESULT_HIT : resultStatus;
    }

    private void validateFinalResult(List<BizDelayedQueryResult> results, String resultStatus, String resultMessage)
    {
        if (!RESULT_HIT.equals(resultStatus) && !RESULT_NO_RESULT.equals(resultStatus) && !RESULT_PARTIAL.equals(resultStatus))
        {
            throw new IllegalArgumentException("resultStatus is invalid");
        }
        if (RESULT_HIT.equals(resultStatus) && !hasValidResults(results))
        {
            throw new IllegalArgumentException("hit result requires at least one detail row");
        }
        if (!RESULT_HIT.equals(resultStatus) && empty(resultMessage))
        {
            throw new IllegalArgumentException("empty or partial result requires resultMessage");
        }
    }

    private boolean hasValidResults(List<BizDelayedQueryResult> results)
    {
        if (results == null)
        {
            return false;
        }
        for (BizDelayedQueryResult result : results)
        {
            if (result != null && !empty(result.getRawJson()) && !isCoverageResult(result))
            {
                return true;
            }
        }
        return false;
    }

    private Billing calculateBilling(Long companyId, String resultStatus)
    {
        return calculateBilling(companyId, resultStatus, currentBillingMonth());
    }

    private Billing calculateBilling(Long companyId, String resultStatus, String billingMonth)
    {
        BizCompanyQueryPrice price = priceMapper == null ? null : priceMapper.selectActivePrice(companyId, BILLING_QUERY_TYPE);
        if (price == null)
        {
            return new Billing(ZERO_FEE, ZERO_FEE, billingMonth, null);
        }
        BigDecimal hitFee = nvl(price.getHitFee());
        BigDecimal noResultFee = nvl(price.getNoResultFee());
        BigDecimal actualFee = RESULT_HIT.equals(resultStatus) ? hitFee : ZERO_FEE;
        BigDecimal reservedFee = hitFee.compareTo(noResultFee) >= 0 ? hitFee : noResultFee;
        return new Billing(actualFee, reservedFee, billingMonth, price.getId());
    }

    private void reserveMonthlyUsage(Long companyId, String billingMonth, BigDecimal reserveFee)
    {
        if (monthlyUsageMapper == null || reserveFee.compareTo(ZERO_FEE) <= 0)
        {
            return;
        }
        BizInsuranceCompany company = companyMapper == null ? null : companyMapper.selectBizInsuranceCompanyById(companyId);
        if (company == null || !"0".equals(company.getBudgetEnabled()) || company.getMonthlyBudget() == null)
        {
            return;
        }
        BigDecimal monthlyBudget = nvl(company.getMonthlyBudget());
        monthlyUsageMapper.ensureUsage(companyId, billingMonth, monthlyBudget);
        int reserved = monthlyUsageMapper.reserveBudget(companyId, billingMonth, monthlyBudget, reserveFee);
        if (reserved <= 0)
        {
            throw new MedicalQueryException("4001", "本月服务额度已达上限");
        }
    }

    private void confirmMonthlyUsage(Long companyId, String billingMonth, BigDecimal reserveFee, BigDecimal fee)
    {
        if (monthlyUsageMapper == null)
        {
            return;
        }
        BizInsuranceCompany company = companyMapper == null ? null : companyMapper.selectBizInsuranceCompanyById(companyId);
        if (company == null || !"0".equals(company.getBudgetEnabled()) || company.getMonthlyBudget() == null)
        {
            return;
        }
        monthlyUsageMapper.ensureUsage(companyId, billingMonth, company.getMonthlyBudget());
        monthlyUsageMapper.confirmBudget(companyId, billingMonth, nvl(reserveFee), fee);
    }

    private String currentBillingMonth()
    {
        return YearMonth.now().toString();
    }

    private void insertBillingLog(BizDelayedQueryRequest request, String resultStatus, Billing billing)
    {
        BizQueryLog log = new BizQueryLog();
        log.setCompanyId(request.getCompanyId());
        log.setQueryType(BILLING_QUERY_TYPE);
        log.setQueryParams(JSON.toJSONString(Map.of(
                "requestNo", request.getRequestNo(),
                "name", request.getPatientName(),
                "idCard", request.getIdCard())));
        log.setFee(billing.fee);
        log.setBillingMonth(billing.billingMonth);
        log.setResultStatus(resultStatus);
        log.setFeeSnapshot(billing.fee);
        log.setPriceConfigId(billing.priceConfigId);
        log.setStatus("0");
        log.setRequestIp(request.getRequestIp());
        log.setRequestTime(new Date());
        log.setRemark("delayed query completed");
        queryLogMapper.insertBizQueryLog(log);
    }

    private List<BizDelayedQueryResult> parseExcel(MultipartFile file, String operator, String queryType) throws Exception
    {
        List<BizDelayedQueryResult> results = new ArrayList<>();
        try (InputStream input = file.getInputStream(); Workbook workbook = WorkbookFactory.create(input))
        {
            Sheet sheet = workbook.getSheetAt(0);
            String normalizedQueryType = QUERY_TYPE_BIG_DATA.equals(queryType) ? QUERY_TYPE_BIG_DATA : QUERY_TYPE_MEDICAL;
            int headerRowIndex = findHeaderRow(sheet, normalizedQueryType);
            if (headerRowIndex < 0)
            {
                throw new IllegalArgumentException(QUERY_TYPE_BIG_DATA.equals(normalizedQueryType)
                        ? "上传文件与大数据查询模板不匹配"
                        : "上传文件与医保查询模板不匹配");
            }
            Map<String, Integer> headers = readHeaders(sheet.getRow(headerRowIndex));
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                if (row == null || !hasMeaningfulData(row, headers, normalizedQueryType))
                {
                    continue;
                }
                Map<String, String> output = QUERY_TYPE_BIG_DATA.equals(normalizedQueryType)
                        ? mapBigDataRow(row, headers)
                        : mapMedicalRow(row, headers);
                BizDelayedQueryResult result = new BizDelayedQueryResult();
                result.setCreateBy(operator);
                result.setRawJson(JSON.toJSONString(output));
                results.add(result);
            }
        }
        return results;
    }

    private int findHeaderRow(Sheet sheet, String queryType)
    {
        int lastCandidate = Math.min(sheet.getLastRowNum(), 9);
        for (int i = 0; i <= lastCandidate; i++)
        {
            Map<String, Integer> headers = readHeaders(sheet.getRow(i));
            if (QUERY_TYPE_BIG_DATA.equals(queryType))
            {
                if (headers.containsKey("姓名") && headers.containsKey("身份证号码")
                        && headers.containsKey("就诊医院") && headers.containsKey("诊断"))
                {
                    return i;
                }
            }
            else if (headers.containsKey("就诊时间") && headers.containsKey("病种名称")
                    && headers.containsKey("定点医药机构名称"))
            {
                return i;
            }
        }
        return -1;
    }

    private Map<String, Integer> readHeaders(Row row)
    {
        Map<String, Integer> headers = new LinkedHashMap<>();
        if (row == null)
        {
            return headers;
        }
        int colCount = Math.max(row.getLastCellNum(), 0);
        for (int i = 0; i < colCount; i++)
        {
            String header = normalizeHeader(cellValue(row.getCell(i)));
            if (!empty(header) && !headers.containsKey(header))
            {
                headers.put(header, i);
            }
        }
        return headers;
    }

    private String normalizeHeader(String value)
    {
        return value == null ? "" : value.replace("\uFEFF", "").replace("\r", "")
                .replace("\n", "").trim();
    }

    private boolean hasMeaningfulData(Row row, Map<String, Integer> headers, String queryType)
    {
        String[] fields = QUERY_TYPE_BIG_DATA.equals(queryType)
                ? new String[] { "姓名", "身份证号码", "就诊医院", "日期", "门诊/住院/体检", "诊断" }
                : new String[] { "定点医药机构名称", "就诊时间", "病种名称", "结束时间" };
        for (String field : fields)
        {
            if (!empty(sourceValue(row, headers, field)))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isCoverageResult(BizDelayedQueryResult result)
    {
        if (result == null || empty(result.getRawJson()))
        {
            return false;
        }
        if (result.getRawJson().contains(COVERAGE_RECORD_MARKER))
        {
            return true;
        }
        try
        {
            return "INSURANCE_COVERAGE".equals(JSON.parseObject(result.getRawJson()).getString("__recordType"));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private Map<String, String> mapMedicalRow(Row row, Map<String, Integer> headers)
    {
        String visitTime = sourceValue(row, headers, "就诊时间");
        String endTime = sourceValue(row, headers, "结束时间");
        String diseaseName = sourceValue(row, headers, "病种名称");
        Map<String, String> output = new LinkedHashMap<>();
        output.put("定点医药机构名称", sourceValue(row, headers, "定点医药机构名称"));
        output.put("就诊时间", visitTime);
        output.put("就诊类型", deriveVisitType(diseaseName, visitTime, endTime));
        output.put("诊断结果", diseaseName);
        output.put("是否报销", deriveReimbursed(sourceValue(row, headers, "有效标志", "有效标识")));
        output.put("结束时间", endTime);
        return output;
    }

    private Map<String, String> mapBigDataRow(Row row, Map<String, Integer> headers)
    {
        Map<String, String> output = new LinkedHashMap<>();
        output.put("姓名", sourceValue(row, headers, "姓名"));
        output.put("性别", sourceValue(row, headers, "性别"));
        output.put("身份证号码", sourceValue(row, headers, "身份证号码"));
        output.put("就诊医院", sourceValue(row, headers, "就诊医院"));
        output.put("日期", sourceValue(row, headers, "日期"));
        output.put("门诊/住院/体检", sourceValue(row, headers, "门诊/住院/体检"));
        output.put("医嘱", sourceValue(row, headers, "医嘱"));
        output.put("诊断", sourceValue(row, headers, "诊断"));
        return output;
    }

    private String sourceValue(Row row, Map<String, Integer> headers, String... names)
    {
        for (String name : names)
        {
            Integer column = headers.get(name);
            if (column != null)
            {
                return cellValue(row.getCell(column)).trim();
            }
        }
        return "";
    }

    private String deriveReimbursed(String validFlag)
    {
        if (empty(validFlag))
        {
            return "";
        }
        String value = validFlag.trim();
        return "有效".equals(value) || "1".equals(value) || "是".equals(value)
                || "Y".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value) ? "是" : "否";
    }

    private String deriveVisitType(String diseaseName, String visitTime, String endTime)
    {
        if (!empty(diseaseName) && diseaseName.contains("门诊"))
        {
            return "门诊";
        }
        LocalDate visitDate = dateOnly(visitTime);
        LocalDate endDate = dateOnly(endTime);
        if (visitDate == null || endDate == null)
        {
            return "";
        }
        return visitDate.equals(endDate) ? "门诊" : "住院";
    }

    private LocalDate dateOnly(String value)
    {
        if (empty(value))
        {
            return null;
        }
        Matcher matcher = DATE_PREFIX.matcher(value.trim());
        if (!matcher.find())
        {
            return null;
        }
        try
        {
            return LocalDate.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)));
        }
        catch (DateTimeException | NumberFormatException e)
        {
            return null;
        }
    }

    private String cellValue(Cell cell)
    {
        if (cell == null)
        {
            return "";
        }
        CellType type = cell.getCellType();
        if (type == CellType.STRING)
        {
            return cell.getStringCellValue();
        }
        if (type == CellType.NUMERIC)
        {
            if (DateUtil.isCellDateFormatted(cell))
            {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
            }
            double value = cell.getNumericCellValue();
            if (value == Math.floor(value))
            {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
        if (type == CellType.BOOLEAN)
        {
            return String.valueOf(cell.getBooleanCellValue());
        }
        if (type == CellType.FORMULA)
        {
            try
            {
                return cell.getStringCellValue();
            }
            catch (Exception e)
            {
                return String.valueOf(cell.getNumericCellValue());
            }
        }
        return "";
    }

    private String newRequestNo()
    {
        return "DQ" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String normalizeQueryType(String queryType)
    {
        if (QUERY_TYPE_MEDICAL.equals(queryType) || QUERY_TYPE_BIG_DATA.equals(queryType))
        {
            return queryType;
        }
        throw new IllegalArgumentException("查询类型仅支持医保查询或大数据查询");
    }

    private boolean empty(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? ZERO_FEE : value;
    }

    private static class Billing
    {
        private final BigDecimal fee;
        private final BigDecimal reservedFee;
        private final String billingMonth;
        private final Long priceConfigId;

        private Billing(BigDecimal fee, BigDecimal reservedFee, String billingMonth, Long priceConfigId)
        {
            this.fee = fee;
            this.reservedFee = reservedFee;
            this.billingMonth = billingMonth;
            this.priceConfigId = priceConfigId;
        }
    }

    public static class CompanyLogs
    {
        private final List<BizQueryLog> realtimeLogs;
        private final List<BizDelayedQueryRequest> delayedLogs;

        public CompanyLogs(List<BizQueryLog> realtimeLogs, List<BizDelayedQueryRequest> delayedLogs)
        {
            this.realtimeLogs = realtimeLogs == null ? new ArrayList<>() : realtimeLogs;
            this.delayedLogs = delayedLogs == null ? new ArrayList<>() : delayedLogs;
        }

        public List<BizQueryLog> getRealtimeLogs() { return realtimeLogs; }
        public List<BizDelayedQueryRequest> getDelayedLogs() { return delayedLogs; }
    }
}
