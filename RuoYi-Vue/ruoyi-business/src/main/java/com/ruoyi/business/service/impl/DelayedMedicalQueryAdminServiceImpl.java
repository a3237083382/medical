package com.ruoyi.business.service.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizMedicalQueryResult;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.domain.medical.DelayedMedicalQueryAdminDetail;
import com.ruoyi.business.domain.medical.DelayedMedicalQueryResultCommand;
import com.ruoyi.business.domain.medical.DelayedMedicalQueryBatchDetail;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMedicalQueryRequestMapper;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMedicalQueryResultMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.mapper.BizQueryLogMapper;
import com.ruoyi.business.service.IDelayedMedicalQueryAdminService;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.business.util.DesensitizeUtil;

@Service
public class DelayedMedicalQueryAdminServiceImpl implements IDelayedMedicalQueryAdminService
{
    private static final String DELAYED_QUERY_TYPE = "precision_delayed";
    private static final BigDecimal ZERO_FEE = BigDecimal.ZERO.setScale(2);
    private static final int MAX_RESULT_ROWS = 5000;
    private static final int MAX_RESULT_COLUMNS = 100;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_RESULT_JSON_LENGTH = 2 * 1024 * 1024;

    private final BizMedicalQueryRequestMapper requestMapper;
    private final BizMedicalQueryBatchMapper batchMapper;
    private final BizMedicalQueryResultMapper resultMapper;
    private final BizInsuranceCompanyMapper companyMapper;
    private final BizMonthlyUsageMapper monthlyUsageMapper;
    private final BizQueryLogMapper queryLogMapper;
    private final TransactionTemplate transactionTemplate;

    public DelayedMedicalQueryAdminServiceImpl(BizMedicalQueryRequestMapper requestMapper,
            BizMedicalQueryBatchMapper batchMapper,
            BizMedicalQueryResultMapper resultMapper, BizInsuranceCompanyMapper companyMapper,
            BizMonthlyUsageMapper monthlyUsageMapper, BizQueryLogMapper queryLogMapper,
            PlatformTransactionManager transactionManager)
    {
        this.requestMapper = requestMapper;
        this.batchMapper = batchMapper;
        this.resultMapper = resultMapper;
        this.companyMapper = companyMapper;
        this.monthlyUsageMapper = monthlyUsageMapper;
        this.queryLogMapper = queryLogMapper;
        this.transactionTemplate = transactionManager == null ? null : new TransactionTemplate(transactionManager);
    }

    @Override
    public List<BizMedicalQueryRequest> selectList(BizMedicalQueryRequest request)
    {
        return requestMapper.selectDelayedRequestList(request);
    }

    @Override
    public List<BizMedicalQueryBatch> selectBatchList(BizMedicalQueryBatch batch)
    {
        return batchMapper.selectDelayedBatchList(batch);
    }

    @Override
    public DelayedMedicalQueryBatchDetail getBatchDetail(Long id)
    {
        BizMedicalQueryBatch batch = batchMapper.selectDelayedBatchById(id);
        if (batch == null)
        {
            throw new MedicalQueryException("4042", "batch not found");
        }
        DelayedMedicalQueryBatchDetail detail = new DelayedMedicalQueryBatchDetail();
        detail.setBatch(batch);
        detail.setItems(batchMapper.selectDelayedBatchItems(id));
        return detail;
    }

    @Override
    public DelayedMedicalQueryAdminDetail getDetail(Long id)
    {
        BizMedicalQueryRequest request = requestMapper.selectDelayedRequestById(id);
        if (request == null)
        {
            throw new MedicalQueryException("4041", "request not found");
        }
        return buildDetail(request, resultMapper.selectByRequestId(id));
    }

    @Override
    public void start(Long id)
    {
        executeInTransaction(() -> {
            if (requestMapper.markDelayedProcessing(id) != 1)
            {
                requireRequestAndState(id, "PENDING", "NOT_UPLOADED");
                throw new MedicalQueryException("4091", "request is not pending");
            }
            return null;
        });
    }

    @Override
    public DelayedMedicalQueryResultCommand previewExcel(Long id, MultipartFile file)
    {
        BizMedicalQueryRequest request = requestMapper.selectDelayedRequestById(id);
        requireState(request, "PROCESSING", "NOT_UPLOADED");
        validateFile(file);
        try (InputStream input = file.getInputStream(); Workbook workbook = WorkbookFactory.create(input))
        {
            if (workbook.getNumberOfSheets() == 0)
            {
                throw new MedicalQueryException("4000", "Excel does not contain a worksheet");
            }
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null || headerRow.getLastCellNum() <= 0)
            {
                throw new MedicalQueryException("4000", "Excel header is empty");
            }
            if (headerRow.getLastCellNum() > MAX_RESULT_COLUMNS)
            {
                throw new MedicalQueryException("4000", "Excel result exceeds 100 columns");
            }
            int columnCount = headerRow.getLastCellNum();
            DataFormatter formatter = new DataFormatter(Locale.CHINA);
            List<Map<String, Object>> schema = new ArrayList<>();
            for (int index = 0; index < columnCount; index++)
            {
                String label = formatter.formatCellValue(headerRow.getCell(index)).trim();
                if (label.isEmpty())
                {
                    label = "字段" + (index + 1);
                }
                Map<String, Object> column = new LinkedHashMap<>();
                column.put("field", "c" + (index + 1));
                column.put("label", label);
                column.put("order", index);
                schema.add(column);
            }

            List<Map<String, Object>> records = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++)
            {
                Row row = sheet.getRow(rowIndex);
                Map<String, Object> record = new LinkedHashMap<>();
                boolean empty = true;
                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
                {
                    String value = row == null ? "" : formatter.formatCellValue(row.getCell(columnIndex)).trim();
                    record.put("c" + (columnIndex + 1), value);
                    empty = empty && value.isEmpty();
                }
                if (!empty)
                {
                    records.add(record);
                    if (records.size() > MAX_RESULT_ROWS)
                    {
                        throw new MedicalQueryException("4000", "Excel result exceeds 5000 rows");
                    }
                }
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("records", records);
            DelayedMedicalQueryResultCommand preview = new DelayedMedicalQueryResultCommand();
            preview.setResultStatus(records.isEmpty() ? "NO_RESULT" : "HIT");
            preview.setColumnSchema(schema);
            preview.setData(data);
            preview.setResultSummary(records.isEmpty() ? "未导入有效结果" : "已导入 " + records.size() + " 条结果");
            return preview;
        }
        catch (MedicalQueryException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new MedicalQueryException("4000", "Excel result cannot be parsed");
        }
    }

    @Override
    public void saveDraft(Long id, DelayedMedicalQueryResultCommand command, String username)
    {
        validateResultContent(command, false);
        executeInTransaction(() -> {
            BizMedicalQueryRequest request = lockAndRequireState(id, "PROCESSING", "NOT_UPLOADED");
            BizMedicalQueryResult existing = resultMapper.selectByRequestId(request.getId());
            BizMedicalQueryResult result = buildStoredResult(request.getId(), command, username, false);
            if (existing == null)
            {
                requireUpdated(resultMapper.insertBizMedicalQueryResult(result), "save delayed draft");
            }
            else
            {
                requireUpdated(resultMapper.updateDraft(result), "update delayed draft");
            }
            return null;
        });
    }

    @Override
    public void complete(Long id, DelayedMedicalQueryResultCommand command, String username)
    {
        validateResultContent(command, true);
        executeInTransaction(() -> {
            BizMedicalQueryRequest request = lockAndRequireState(id, "PROCESSING", "NOT_UPLOADED");
            BizMedicalQueryResult existing = resultMapper.selectByRequestId(request.getId());
            BizMedicalQueryResult result = buildStoredResult(request.getId(), command, username, true);
            if (existing == null)
            {
                requireUpdated(resultMapper.insertBizMedicalQueryResult(result), "upload delayed result");
            }
            else
            {
                requireUpdated(resultMapper.uploadResult(result), "upload delayed draft");
            }

            BigDecimal actualFee = "HIT".equals(command.getResultStatus()) ? nvl(request.getReservedFee()) : ZERO_FEE;
            BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyById(request.getCompanyId());
            if (company != null && isMonthlyBudgetEnabled(company))
            {
                requireUpdated(monthlyUsageMapper.confirmBudget(request.getCompanyId(), request.getBillingMonth(),
                        nvl(request.getReservedFee()), actualFee), "confirm delayed query fee");
            }
            BizQueryLog log = insertQueryLog(request, command.getResultStatus(), actualFee);
            requireUpdated(requestMapper.completeDelayedRequest(request.getId(), command.getResultStatus(), actualFee,
                    log.getId()), "complete delayed request");
            return null;
        });
    }

    @Override
    public void updateUploaded(Long id, DelayedMedicalQueryResultCommand command, String username)
    {
        validateResultContent(command, false);
        if (isEmpty(command.getUpdateReason()) || command.getUpdateReason().trim().length() > 500)
        {
            throw new MedicalQueryException("4000", "updateReason is required and must not exceed 500 characters");
        }
        executeInTransaction(() -> {
            BizMedicalQueryRequest request = lockAndRequireState(id, "COMPLETED", "UPLOADED");
            if (!isEmpty(command.getResultStatus()) && !request.getResultStatus().equals(command.getResultStatus()))
            {
                throw new MedicalQueryException("4091", "uploaded result status cannot be changed");
            }
            BizMedicalQueryResult existing = resultMapper.selectByRequestId(request.getId());
            if (existing == null)
            {
                throw new MedicalQueryException("4091", "uploaded result is missing");
            }
            BizMedicalQueryResult result = buildStoredResult(request.getId(), command, username, false);
            result.setUpdateReason(command.getUpdateReason().trim());
            requireUpdated(resultMapper.updateUploadedResult(result), "update uploaded result");
            return null;
        });
    }

    private BizMedicalQueryResult buildStoredResult(Long requestId, DelayedMedicalQueryResultCommand command,
            String username, boolean uploaded)
    {
        BizMedicalQueryResult result = new BizMedicalQueryResult();
        result.setRequestId(requestId);
        result.setResultSource("MANUAL");
        result.setColumnSchema(JSON.toJSONString(command.getColumnSchema()));
        result.setResultData(JSON.toJSONString(command.getData()));
        result.setResultSummary(normalize(command.getResultSummary()));
        result.setVersion(1);
        result.setUpdateBy(username);
        result.setUpdateReason(normalize(command.getUpdateReason()));
        if (uploaded)
        {
            result.setUploadedBy(username);
            result.setUploadedTime(new Date());
        }
        return result;
    }

    private BizQueryLog insertQueryLog(BizMedicalQueryRequest request, String resultStatus, BigDecimal actualFee)
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", request.getPatientName());
        params.put("idCard", request.getIdCard());
        BizQueryLog log = new BizQueryLog();
        log.setRequestNo(request.getRequestNo());
        log.setServiceMode("DELAYED");
        log.setEntryType(request.getEntryType());
        log.setCompanyId(request.getCompanyId());
        log.setQueryType(DELAYED_QUERY_TYPE);
        log.setQueryParams(JSON.toJSONString(DesensitizeUtil.desensitize(params)));
        log.setFee(actualFee);
        log.setBillingMonth(request.getBillingMonth());
        log.setResultStatus(resultStatus);
        log.setFeeSnapshot(actualFee);
        log.setPriceConfigId(request.getPriceConfigId());
        log.setStatus("0");
        log.setRequestIp(request.getRequestIp());
        log.setRequestTime(new Date());
        log.setRemark("精准延时结果上传完毕");
        requireUpdated(queryLogMapper.insertBizQueryLog(log), "write delayed query log");
        return log;
    }

    private DelayedMedicalQueryAdminDetail buildDetail(BizMedicalQueryRequest request,
            BizMedicalQueryResult result)
    {
        DelayedMedicalQueryAdminDetail detail = new DelayedMedicalQueryAdminDetail();
        detail.setRequest(request);
        if (result != null)
        {
            detail.setHasResult(true);
            detail.setColumnSchema(parseJson(result.getColumnSchema()));
            detail.setData(parseData(result.getResultData()));
            detail.setResultSummary(result.getResultSummary());
            detail.setResultVersion(result.getVersion());
            detail.setUploadedBy(result.getUploadedBy());
            detail.setUploadedTime(result.getUploadedTime());
            detail.setUpdateBy(result.getUpdateBy());
            detail.setUpdateTime(result.getUpdateTime());
            detail.setUpdateReason(result.getUpdateReason());
        }
        return detail;
    }

    private void validateResultContent(DelayedMedicalQueryResultCommand command, boolean requireStatus)
    {
        if (command == null || command.getColumnSchema() == null || command.getData() == null)
        {
            throw new MedicalQueryException("4000", "columnSchema and data are required");
        }
        if (command.getColumnSchema().isEmpty() || command.getColumnSchema().size() > MAX_RESULT_COLUMNS)
        {
            throw new MedicalQueryException("4000", "result columns must contain 1 to 100 fields");
        }
        Set<String> fields = new HashSet<>();
        for (Map<String, Object> column : command.getColumnSchema())
        {
            String field = column == null ? "" : normalize(column.get("field"));
            String label = column == null ? "" : normalize(column.get("label"));
            if (isEmpty(field) || isEmpty(label) || !fields.add(field))
            {
                throw new MedicalQueryException("4000", "result column field and label must be unique and non-empty");
            }
        }
        if (requireStatus && !List.of("HIT", "NO_RESULT", "HINT_ONLY").contains(command.getResultStatus()))
        {
            throw new MedicalQueryException("4000", "resultStatus must be HIT, NO_RESULT or HINT_ONLY");
        }
        if (requireStatus && "HIT".equals(command.getResultStatus()) && !hasResultData(command.getData()))
        {
            throw new MedicalQueryException("4000", "HIT result must contain result data");
        }
        if (requireStatus && "HINT_ONLY".equals(command.getResultStatus()) && isEmpty(normalize(command.getResultSummary())))
        {
            throw new MedicalQueryException("4000", "HINT_ONLY result must contain a result summary");
        }
        if (JSON.toJSONString(command.getData()).length() > MAX_RESULT_JSON_LENGTH)
        {
            throw new MedicalQueryException("4000", "result data is too large");
        }
    }

    private boolean hasResultData(Map<String, Object> data)
    {
        if (data == null || data.isEmpty())
        {
            return false;
        }
        Object records = data.get("records");
        return !(records instanceof Iterable<?> iterable) || iterable.iterator().hasNext();
    }

    private void validateFile(MultipartFile file)
    {
        String name = file == null ? null : file.getOriginalFilename();
        String lowerName = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (file == null || file.isEmpty() || (!lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls")))
        {
            throw new MedicalQueryException("4000", "only .xlsx or .xls files are supported");
        }
        if (file.getSize() > MAX_FILE_SIZE)
        {
            throw new MedicalQueryException("4000", "Excel file must not exceed 10 MB");
        }
    }

    private BizMedicalQueryRequest lockAndRequireState(Long id, String processStatus, String uploadStatus)
    {
        BizMedicalQueryRequest request = requestMapper.selectDelayedRequestByIdForUpdate(id);
        requireState(request, processStatus, uploadStatus);
        return request;
    }

    private void requireRequestAndState(Long id, String processStatus, String uploadStatus)
    {
        requireState(requestMapper.selectDelayedRequestById(id), processStatus, uploadStatus);
    }

    private void requireState(BizMedicalQueryRequest request, String processStatus, String uploadStatus)
    {
        if (request == null)
        {
            throw new MedicalQueryException("4041", "request not found");
        }
        if (!processStatus.equals(request.getProcessStatus()) || !uploadStatus.equals(request.getUploadStatus()))
        {
            throw new MedicalQueryException("4091", "request state does not allow this operation");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseData(String json)
    {
        return isEmpty(json) ? new LinkedHashMap<>() : JSON.parseObject(json, LinkedHashMap.class);
    }

    private Object parseJson(String json)
    {
        return isEmpty(json) ? null : JSON.parse(json);
    }

    private boolean isMonthlyBudgetEnabled(BizInsuranceCompany company)
    {
        return "0".equals(company.getBudgetEnabled()) && company.getMonthlyBudget() != null;
    }

    private String normalize(Object value)
    {
        return value == null ? null : String.valueOf(value).trim();
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

    private <T> T executeInTransaction(java.util.function.Supplier<T> action)
    {
        return transactionTemplate == null ? action.get() : transactionTemplate.execute(status -> action.get());
    }
}
