package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizMedicalQueryResult;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.BizMedicalQueryBatchItem;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.domain.medical.DelayedMedicalQueryResultCommand;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMedicalQueryRequestMapper;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMedicalQueryResultMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.mapper.BizQueryLogMapper;
import com.ruoyi.business.service.MedicalQueryException;

public class DelayedMedicalQueryAdminServiceImplTest
{
    private BizMedicalQueryRequestMapper requestMapper;
    private BizMedicalQueryBatchMapper batchMapper;
    private BizMedicalQueryResultMapper resultMapper;
    private BizInsuranceCompanyMapper companyMapper;
    private BizMonthlyUsageMapper monthlyUsageMapper;
    private BizQueryLogMapper queryLogMapper;
    private DelayedMedicalQueryAdminServiceImpl service;

    @BeforeEach
    public void setUp()
    {
        requestMapper = mock(BizMedicalQueryRequestMapper.class);
        batchMapper = mock(BizMedicalQueryBatchMapper.class);
        resultMapper = mock(BizMedicalQueryResultMapper.class);
        companyMapper = mock(BizInsuranceCompanyMapper.class);
        monthlyUsageMapper = mock(BizMonthlyUsageMapper.class);
        queryLogMapper = mock(BizQueryLogMapper.class);
        when(requestMapper.markDelayedProcessing(10L)).thenReturn(1);
        when(resultMapper.insertBizMedicalQueryResult(any())).thenReturn(1);
        when(resultMapper.updateDraft(any())).thenReturn(1);
        when(resultMapper.uploadResult(any())).thenReturn(1);
        when(resultMapper.updateUploadedResult(any())).thenReturn(1);
        when(monthlyUsageMapper.confirmBudget(any(), anyString(), any(), any())).thenReturn(1);
        when(queryLogMapper.insertBizQueryLog(any())).thenAnswer(invocation -> {
            BizQueryLog log = invocation.getArgument(0);
            log.setId(99L);
            return 1;
        });
        when(requestMapper.completeDelayedRequest(any(), anyString(), any(), any())).thenReturn(1);
        service = new DelayedMedicalQueryAdminServiceImpl(requestMapper, batchMapper, resultMapper, companyMapper,
                monthlyUsageMapper, queryLogMapper, null);
    }

    @Test
    public void batchListUsesLiveBatchSummaryMapper()
    {
        BizMedicalQueryBatch query = new BizMedicalQueryBatch();
        BizMedicalQueryBatch batch = new BizMedicalQueryBatch();
        batch.setId(20L);
        when(batchMapper.selectDelayedBatchList(query)).thenReturn(List.of(batch));

        assertEquals(20L, service.selectBatchList(query).get(0).getId());
        verify(batchMapper).selectDelayedBatchList(query);
    }

    @Test
    public void batchDetailIncludesOrderedMembers()
    {
        BizMedicalQueryBatch batch = new BizMedicalQueryBatch();
        batch.setId(20L);
        BizMedicalQueryBatchItem item = new BizMedicalQueryBatchItem();
        item.setId(30L);
        when(batchMapper.selectDelayedBatchById(20L)).thenReturn(batch);
        when(batchMapper.selectDelayedBatchItems(20L)).thenReturn(List.of(item));

        var detail = service.getBatchDetail(20L);

        assertEquals(20L, detail.getBatch().getId());
        assertEquals(30L, detail.getItems().get(0).getId());
    }

    @Test
    public void missingBatchReturnsStableErrorCode()
    {
        MedicalQueryException exception = assertThrows(MedicalQueryException.class,
                () -> service.getBatchDetail(404L));

        assertEquals("4042", exception.getCode());
    }

    @Test
    public void startUsesAtomicPendingTransition()
    {
        service.start(10L);

        verify(requestMapper).markDelayedProcessing(10L);
        verify(requestMapper, never()).selectDelayedRequestByIdForUpdate(any());
    }

    @Test
    public void draftIsStoredWithoutUploadingOrBilling()
    {
        BizMedicalQueryRequest request = processingRequest();
        when(requestMapper.selectDelayedRequestByIdForUpdate(10L)).thenReturn(request);

        service.saveDraft(10L, command("HIT"), "admin");

        ArgumentCaptor<BizMedicalQueryResult> captor = ArgumentCaptor.forClass(BizMedicalQueryResult.class);
        verify(resultMapper).insertBizMedicalQueryResult(captor.capture());
        assertEquals("MANUAL", captor.getValue().getResultSource());
        assertEquals("admin", captor.getValue().getUpdateBy());
        assertEquals(null, captor.getValue().getUploadedTime());
        verify(queryLogMapper, never()).insertBizQueryLog(any());
        verify(monthlyUsageMapper, never()).confirmBudget(any(), anyString(), any(), any());
    }

    @Test
    public void excelPreviewBuildsEditableDynamicColumnsAndRows() throws Exception
    {
        BizMedicalQueryRequest request = processingRequest();
        when(requestMapper.selectDelayedRequestById(10L)).thenReturn(request);
        byte[] workbook = workbook();
        MockMultipartFile file = new MockMultipartFile("file", "result.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbook);

        DelayedMedicalQueryResultCommand preview = service.previewExcel(10L, file);

        assertEquals("HIT", preview.getResultStatus());
        assertEquals("姓名", preview.getColumnSchema().get(0).get("label"));
        assertEquals("c1", preview.getColumnSchema().get(0).get("field"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records = (List<Map<String, Object>>) preview.getData().get("records");
        assertEquals(1, records.size());
        assertEquals("张三", records.get(0).get("c1"));
    }

    @Test
    public void hitCompletionConfirmsFeeWritesDesensitizedLogAndCompletesRequest()
    {
        BizMedicalQueryRequest request = processingRequest();
        when(requestMapper.selectDelayedRequestByIdForUpdate(10L)).thenReturn(request);
        BizMedicalQueryResult draft = new BizMedicalQueryResult();
        draft.setRequestId(10L);
        when(resultMapper.selectByRequestId(10L)).thenReturn(draft);
        when(companyMapper.selectBizInsuranceCompanyById(1L)).thenReturn(company());

        service.complete(10L, command("HIT"), "admin");

        verify(resultMapper).uploadResult(any());
        verify(monthlyUsageMapper).confirmBudget(1L, "2026-08", new BigDecimal("20.00"),
                new BigDecimal("20.00"));
        ArgumentCaptor<BizQueryLog> logCaptor = ArgumentCaptor.forClass(BizQueryLog.class);
        verify(queryLogMapper).insertBizQueryLog(logCaptor.capture());
        BizQueryLog log = logCaptor.getValue();
        assertEquals("DELAYED", log.getServiceMode());
        assertEquals("precision_delayed", log.getQueryType());
        assertFalse(log.getQueryParams().contains("430102199001011234"));
        assertTrue(log.getQueryParams().contains("430***********1234"));
        verify(requestMapper).completeDelayedRequest(10L, "HIT", new BigDecimal("20.00"), 99L);
    }

    @Test
    public void noResultCompletionReleasesReservationWithoutCharging()
    {
        BizMedicalQueryRequest request = processingRequest();
        when(requestMapper.selectDelayedRequestByIdForUpdate(10L)).thenReturn(request);
        when(companyMapper.selectBizInsuranceCompanyById(1L)).thenReturn(company());

        service.complete(10L, command("NO_RESULT"), "admin");

        verify(monthlyUsageMapper).confirmBudget(1L, "2026-08", new BigDecimal("20.00"),
                new BigDecimal("0.00"));
        verify(requestMapper).completeDelayedRequest(10L, "NO_RESULT", new BigDecimal("0.00"), 99L);
    }

    @Test
    public void uploadedContentCanBeCorrectedButBillingStatusCannotChange()
    {
        BizMedicalQueryRequest request = processingRequest();
        request.setProcessStatus("COMPLETED");
        request.setUploadStatus("UPLOADED");
        request.setResultStatus("HIT");
        when(requestMapper.selectDelayedRequestByIdForUpdate(10L)).thenReturn(request);
        when(resultMapper.selectByRequestId(10L)).thenReturn(new BizMedicalQueryResult());
        DelayedMedicalQueryResultCommand command = command("NO_RESULT");
        command.setUpdateReason("修正录入字段");

        MedicalQueryException exception = assertThrows(MedicalQueryException.class,
                () -> service.updateUploaded(10L, command, "admin"));

        assertEquals("4091", exception.getCode());
        verify(resultMapper, never()).updateUploadedResult(any());
    }

    private DelayedMedicalQueryResultCommand command(String resultStatus)
    {
        DelayedMedicalQueryResultCommand command = new DelayedMedicalQueryResultCommand();
        command.setResultStatus(resultStatus);
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("field", "diagnosis");
        column.put("label", "诊断");
        column.put("order", 0);
        command.setColumnSchema(List.of(column));
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("diagnosis", "高血压");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", List.of(record));
        command.setData(data);
        command.setResultSummary("查询完成");
        return command;
    }

    private BizMedicalQueryRequest processingRequest()
    {
        BizMedicalQueryRequest request = new BizMedicalQueryRequest();
        request.setId(10L);
        request.setRequestNo("MD20260817TEST");
        request.setCompanyId(1L);
        request.setEntryType("SINGLE");
        request.setServiceMode("DELAYED");
        request.setPatientName("张三");
        request.setIdCard("430102199001011234");
        request.setProcessStatus("PROCESSING");
        request.setUploadStatus("NOT_UPLOADED");
        request.setReservedFee(new BigDecimal("20.00"));
        request.setBillingMonth("2026-08");
        request.setRequestIp("127.0.0.1");
        return request;
    }

    private BizInsuranceCompany company()
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(1L);
        company.setBudgetEnabled("0");
        company.setMonthlyBudget(new BigDecimal("100.00"));
        return company;
    }

    private byte[] workbook() throws Exception
    {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            var sheet = workbook.createSheet("结果");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("姓名");
            header.createCell(1).setCellValue("诊断");
            var row = sheet.createRow(1);
            row.createCell(0).setCellValue("张三");
            row.createCell(1).setCellValue("高血压");
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
