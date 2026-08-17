package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.BizMedicalQueryBatchItem;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizMedicalQueryResult;
import com.ruoyi.business.domain.medical.MedicalQueryExportFile;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMedicalQueryRequestMapper;
import com.ruoyi.business.mapper.BizMedicalQueryResultMapper;
import com.ruoyi.business.service.MedicalQueryException;

public class DelayedMedicalQueryExportServiceImplTest
{
    private BizMedicalQueryRequestMapper requestMapper;
    private BizMedicalQueryBatchMapper batchMapper;
    private BizMedicalQueryResultMapper resultMapper;
    private DelayedMedicalQueryExportServiceImpl service;

    @BeforeEach
    public void setUp()
    {
        requestMapper = mock(BizMedicalQueryRequestMapper.class);
        batchMapper = mock(BizMedicalQueryBatchMapper.class);
        resultMapper = mock(BizMedicalQueryResultMapper.class);
        service = new DelayedMedicalQueryExportServiceImpl(requestMapper, batchMapper, resultMapper);
    }

    @Test
    public void singleExportContainsMetadataAndDynamicRows()
            throws Exception
    {
        BizMedicalQueryRequest request = request("REQ-1", 1L, "COMPLETED", "UPLOADED");
        BizMedicalQueryResult result = result("[{\"field\":\"hospital\",\"label\":\"医疗机构\",\"order\":0},"
                + "{\"field\":\"visitDate\",\"label\":\"就诊日期\",\"order\":1}]",
                "{\"records\":[{\"hospital\":\"人民医院\",\"visitDate\":\"2026-08-17\"}]}");
        when(requestMapper.selectCompanyRequestByNo(1L, "REQ-1")).thenReturn(request);
        when(resultMapper.selectByRequestId(1L)).thenReturn(result);

        MedicalQueryExportFile file = service.exportRequest(1L, "REQ-1");

        try (Workbook workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(file.getContent())))
        {
            assertEquals("查询结果", workbook.getSheetName(0));
            assertEquals("@", workbook.getSheetAt(0).getRow(4).getCell(1).getCellStyle().getDataFormatString());
            assertEquals(true, workbook.getSheetAt(0).getRow(4).getCell(1).getCellStyle().getQuotePrefixed());
            assertEquals("医疗机构", workbook.getSheetAt(0).getRow(10).getCell(0).getStringCellValue());
            assertEquals("人民医院", workbook.getSheetAt(0).getRow(11).getCell(0).getStringCellValue());
            assertEquals("2026-08-17", workbook.getSheetAt(0).getRow(11).getCell(1).getStringCellValue());
        }
    }

    @Test
    public void singleExportRejectsDraftAndCrossCompanyLookup()
    {
        when(requestMapper.selectCompanyRequestByNo(2L, "REQ-1")).thenReturn(null);

        MedicalQueryException exception = assertThrows(MedicalQueryException.class,
                () -> service.exportRequest(2L, "REQ-1"));

        assertEquals("4041", exception.getCode());
        verify(requestMapper).selectCompanyRequestByNo(2L, "REQ-1");
    }

    @Test
    public void failedRequestExportsStatusWithoutLeakingDraft()
            throws Exception
    {
        BizMedicalQueryRequest request = request("REQ-FAILED", 3L, "FAILED", "NOT_UPLOADED");
        request.setResultStatus("FAILED");
        BizMedicalQueryResult draft = result("[{\"field\":\"draft\",\"label\":\"草稿\"}]",
                "{\"records\":[{\"draft\":\"不可见\"}]}");
        draft.setUploadedTime(null);
        when(requestMapper.selectCompanyRequestByNo(1L, "REQ-FAILED")).thenReturn(request);
        when(resultMapper.selectByRequestId(3L)).thenReturn(draft);

        MedicalQueryExportFile file = service.exportRequest(1L, "REQ-FAILED");

        try (Workbook workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(file.getContent())))
        {
            assertEquals("查询结果", workbook.getSheetName(0));
            assertEquals("FAILED", workbook.getSheetAt(0).getRow(11).getCell(0).getStringCellValue());
        }
    }

    @Test
    public void batchExportRejectsPendingItemsBeforeReadingResults()
    {
        BizMedicalQueryBatch batch = new BizMedicalQueryBatch();
        batch.setId(7L);
        when(batchMapper.selectCompanyBatchByNo(1L, "B-1")).thenReturn(batch);
        BizMedicalQueryBatchItem pending = item(1, "PENDING", "NOT_UPLOADED");
        when(batchMapper.selectDelayedBatchItems(7L)).thenReturn(List.of(pending));

        MedicalQueryException exception = assertThrows(MedicalQueryException.class,
                () -> service.exportBatch(1L, "B-1"));

        assertEquals("4094", exception.getCode());
    }

    @Test
    public void batchExportKeepsDifferentSchemasOnSeparateSheets()
            throws Exception
    {
        BizMedicalQueryBatch batch = new BizMedicalQueryBatch();
        batch.setId(7L);
        batch.setBatchNo("B-1");
        when(batchMapper.selectCompanyBatchByNo(1L, "B-1")).thenReturn(batch);
        BizMedicalQueryBatchItem first = item(1, "COMPLETED", "UPLOADED");
        first.setRequestId(1L);
        first.setPatientName("甲");
        BizMedicalQueryBatchItem second = item(2, "COMPLETED", "UPLOADED");
        second.setRequestId(2L);
        second.setPatientName("乙");
        when(batchMapper.selectDelayedBatchItems(7L)).thenReturn(List.of(first, second));
        when(requestMapper.selectDelayedRequestById(1L)).thenReturn(request("REQ-1", 1L, "COMPLETED", "UPLOADED"));
        when(requestMapper.selectDelayedRequestById(2L)).thenReturn(request("REQ-2", 1L, "COMPLETED", "UPLOADED"));
        when(resultMapper.selectByRequestId(1L)).thenReturn(result("[{\"field\":\"a\",\"label\":\"字段A\",\"order\":0}]",
                "{\"records\":[{\"a\":\"A\"}]}"));
        when(resultMapper.selectByRequestId(2L)).thenReturn(result("[{\"field\":\"b\",\"label\":\"字段B\",\"order\":0}]",
                "{\"records\":[{\"b\":\"B\"}]}"));

        MedicalQueryExportFile file = service.exportBatch(1L, "B-1");
        try (Workbook workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(file.getContent())))
        {
            assertEquals(3, workbook.getNumberOfSheets());
            assertEquals("字段A", workbook.getSheetAt(1).getRow(10).getCell(0).getStringCellValue());
            assertEquals("字段B", workbook.getSheetAt(2).getRow(10).getCell(0).getStringCellValue());
        }
    }

    private BizMedicalQueryRequest request(String no, Long id, String process, String upload)
    {
        BizMedicalQueryRequest request = new BizMedicalQueryRequest();
        request.setId(id);
        request.setRequestNo(no);
        request.setCompanyId(1L);
        request.setServiceMode("DELAYED");
        request.setPatientName("张三");
        request.setIdCard("430102199001011234");
        request.setProcessStatus(process);
        request.setUploadStatus(upload);
        request.setResultStatus("HIT");
        request.setCompleteTime(new Date());
        return request;
    }

    private BizMedicalQueryResult result(String schema, String data)
    {
        BizMedicalQueryResult result = new BizMedicalQueryResult();
        result.setRequestId(1L);
        result.setColumnSchema(schema);
        result.setResultData(data);
        result.setResultSummary("已上传");
        result.setUploadedTime(new Date());
        return result;
    }

    private BizMedicalQueryBatchItem item(int row, String process, String upload)
    {
        BizMedicalQueryBatchItem item = new BizMedicalQueryBatchItem();
        item.setRowNo(row);
        item.setProcessStatus(process);
        item.setUploadStatus(upload);
        item.setResultStatus("HIT");
        item.setFeeSnapshot(BigDecimal.ZERO);
        return item;
    }
}
