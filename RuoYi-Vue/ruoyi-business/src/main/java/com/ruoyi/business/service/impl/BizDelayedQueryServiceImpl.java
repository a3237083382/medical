package com.ruoyi.business.service.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private static final BigDecimal ZERO_FEE = new BigDecimal("0.00");

    public static final String QUERY_TYPE = "delayed_precise";
    public static final String QUERY_PENDING = "PENDING";
    public static final String QUERY_QUERIED = "QUERIED";
    public static final String UPLOAD_NOT_UPLOADED = "NOT_UPLOADED";
    public static final String UPLOAD_UPLOADED = "UPLOADED";
    public static final String RESULT_HIT = "HIT";
    public static final String RESULT_NO_RESULT = "NO_RESULT";
    public static final String RESULT_PARTIAL = "PARTIAL";

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
        if (empty(patientName) || empty(idCard))
        {
            throw new IllegalArgumentException("patientName and idCard are required");
        }
        BizDelayedQueryRequest duplicate = requestMapper.selectPendingDuplicate(companyId, patientName, idCard);
        if (duplicate != null)
        {
            duplicate.setResults(new ArrayList<>());
            return duplicate;
        }
        BizDelayedQueryRequest request = new BizDelayedQueryRequest();
        Billing reservation = calculateBilling(companyId, RESULT_HIT);
        reserveMonthlyUsage(companyId, reservation.billingMonth, reservation.reservedFee);
        request.setRequestNo(newRequestNo());
        request.setCompanyId(companyId);
        request.setCompanyNameSnapshot(companyName);
        request.setPatientName(patientName);
        request.setIdCard(idCard);
        request.setQueryType(QUERY_TYPE);
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
        if (requests == null || requests.isEmpty())
        {
            throw new IllegalArgumentException("items are required");
        }
        List<BizDelayedQueryRequest> submitted = new ArrayList<>();
        for (BizDelayedQueryRequest item : requests)
        {
            if (item == null)
            {
                throw new IllegalArgumentException("patientName and idCard are required");
            }
            submitted.add(submit(companyId, companyName, item.getPatientName(), item.getIdCard(), requestIp));
        }
        return submitted;
    }

    @Override
    public List<BizDelayedQueryRequest> selectList(BizDelayedQueryRequest request)
    {
        return requestMapper.selectBizDelayedQueryRequestList(request);
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
        List<BizDelayedQueryResult> results = parseExcel(file, operator);
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
            if (result != null && !empty(result.getRawJson()))
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
        BizCompanyQueryPrice price = priceMapper == null ? null : priceMapper.selectActivePrice(companyId, QUERY_TYPE);
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
        log.setQueryType(QUERY_TYPE);
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

    private List<BizDelayedQueryResult> parseExcel(MultipartFile file, String operator) throws Exception
    {
        List<BizDelayedQueryResult> results = new ArrayList<>();
        try (InputStream input = file.getInputStream(); Workbook workbook = WorkbookFactory.create(input))
        {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = new ArrayList<>();
            for (int i = 0; i <= sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                if (row == null)
                {
                    continue;
                }
                int colCount = Math.max(row.getLastCellNum(), 0);
                if (i == 0)
                {
                    for (int j = 0; j < colCount; j++)
                    {
                        headers.add(cellValue(row.getCell(j)));
                    }
                    continue;
                }
                Map<String, String> raw = new LinkedHashMap<>();
                boolean hasValue = false;
                for (int j = 0; j < headers.size(); j++)
                {
                    String header = headers.get(j);
                    if (empty(header))
                    {
                        header = "column_" + (j + 1);
                    }
                    String value = cellValue(row.getCell(j));
                    if (!empty(value))
                    {
                        hasValue = true;
                    }
                    raw.put(header, value);
                }
                if (hasValue)
                {
                    BizDelayedQueryResult result = new BizDelayedQueryResult();
                    result.setCreateBy(operator);
                    result.setRawJson(JSON.toJSONString(raw));
                    results.add(result);
                }
            }
        }
        return results;
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
