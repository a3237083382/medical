package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.medical.MedicalQueryBatchCancellationResult;
import com.ruoyi.business.domain.medical.MedicalQueryBatchItemState;
import com.ruoyi.business.domain.medical.MedicalQueryBatchProgress;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.service.MedicalQueryException;

public class MedicalQueryBatchCancellationServiceImplTest
{
    private BizMedicalQueryBatchMapper batchMapper;
    private BizInsuranceCompanyMapper companyMapper;
    private BizMonthlyUsageMapper monthlyUsageMapper;
    private MedicalQueryBatchCancellationServiceImpl service;

    @BeforeEach
    public void setUp()
    {
        batchMapper = mock(BizMedicalQueryBatchMapper.class);
        companyMapper = mock(BizInsuranceCompanyMapper.class);
        monthlyUsageMapper = mock(BizMonthlyUsageMapper.class);
        when(companyMapper.selectBizInsuranceCompanyById(1L)).thenReturn(company());
        when(batchMapper.cancelBatchItem(any())).thenReturn(1);
        when(batchMapper.cancelPendingRequest(any())).thenReturn(1);
        when(batchMapper.updateBatchSummary(any())).thenReturn(1);
        when(monthlyUsageMapper.releaseBudget(any(), any(), any())).thenReturn(1);
        when(batchMapper.selectCompanyBatchProgress(1L, "B001")).thenReturn(progress());
        service = new MedicalQueryBatchCancellationServiceImpl(batchMapper, companyMapper, monthlyUsageMapper, null);
    }

    @Test
    public void reusedItemOnlyCancelsCurrentBatchLink()
    {
        MedicalQueryBatchItemState item = item(11L, "1", "PENDING");
        when(batchMapper.selectCompanyBatchItemForUpdate(1L, 11L)).thenReturn(item);

        MedicalQueryBatchCancellationResult result = service.cancelItem(1L, 11L);

        assertEquals(1, result.getCancelledCount());
        verify(batchMapper).cancelBatchItem(11L);
        verify(batchMapper, never()).cancelPendingRequest(any());
        verify(monthlyUsageMapper, never()).releaseBudget(any(), any(), any());
    }

    @Test
    public void exclusiveNewRequestIsCancelledAndReservationReleased()
    {
        MedicalQueryBatchItemState item = item(11L, "0", "PENDING");
        when(batchMapper.selectCompanyBatchItemForUpdate(1L, 11L)).thenReturn(item);
        when(batchMapper.countActiveItemsByRequestId(21L)).thenReturn(0);

        service.cancelItem(1L, 11L);

        verify(batchMapper).cancelPendingRequest(21L);
        verify(monthlyUsageMapper).releaseBudget(1L, "2026-08", new BigDecimal("20.00"));
    }

    @Test
    public void requestStillReferencedByAnotherBatchIsNotCancelled()
    {
        MedicalQueryBatchItemState item = item(11L, "0", "PENDING");
        when(batchMapper.selectCompanyBatchItemForUpdate(1L, 11L)).thenReturn(item);
        when(batchMapper.countActiveItemsByRequestId(21L)).thenReturn(1);

        service.cancelItem(1L, 11L);

        verify(batchMapper, never()).cancelPendingRequest(any());
        verify(monthlyUsageMapper, never()).releaseBudget(any(), any(), any());
    }

    @Test
    public void processingItemCannotBeCancelled()
    {
        MedicalQueryBatchItemState item = item(11L, "0", "PROCESSING");
        when(batchMapper.selectCompanyBatchItemForUpdate(1L, 11L)).thenReturn(item);

        MedicalQueryException exception = assertThrows(MedicalQueryException.class,
                () -> service.cancelItem(1L, 11L));

        assertEquals("4092", exception.getCode());
        verify(batchMapper, never()).cancelBatchItem(any());
    }

    @Test
    public void wholeBatchCancelsEligibleItemsAndReportsStartedItems()
    {
        BizMedicalQueryBatch batch = new BizMedicalQueryBatch();
        batch.setId(100L);
        batch.setBatchNo("B001");
        when(batchMapper.selectCompanyBatchByNoForUpdate(1L, "B001")).thenReturn(batch);
        MedicalQueryBatchItemState pending = item(11L, "1", "PENDING");
        MedicalQueryBatchItemState processing = item(12L, "0", "PROCESSING");
        when(batchMapper.selectBatchItemsForUpdate(100L)).thenReturn(List.of(pending, processing));

        MedicalQueryBatchCancellationResult result = service.cancelBatch(1L, "B001");

        assertEquals(1, result.getCancelledCount());
        assertEquals(1, result.getNotCancellableCount());
        verify(batchMapper).cancelBatchItem(11L);
        verify(batchMapper, never()).cancelBatchItem(12L);
    }

    private MedicalQueryBatchItemState item(Long itemId, String reusedFlag, String processStatus)
    {
        MedicalQueryBatchItemState item = new MedicalQueryBatchItemState();
        item.setItemId(itemId);
        item.setBatchId(100L);
        item.setBatchNo("B001");
        item.setRequestId(21L);
        item.setRequestNo("MD001");
        item.setEntryType("BATCH");
        item.setReusedFlag(reusedFlag);
        item.setItemStatus("ACTIVE");
        item.setProcessStatus(processStatus);
        item.setUploadStatus("NOT_UPLOADED");
        item.setReservedFee(new BigDecimal("20.00"));
        item.setBillingMonth("2026-08");
        return item;
    }

    private BizInsuranceCompany company()
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(1L);
        company.setBudgetEnabled("0");
        company.setMonthlyBudget(new BigDecimal("100.00"));
        return company;
    }

    private MedicalQueryBatchProgress progress()
    {
        MedicalQueryBatchProgress progress = new MedicalQueryBatchProgress();
        progress.setBatchId(100L);
        progress.setBatchNo("B001");
        progress.setBatchStatus("PARTIAL_CANCELLED");
        progress.setTotalCount(2);
        progress.setCancelledCount(1);
        progress.setTotalFee(BigDecimal.ZERO.setScale(2));
        return progress;
    }
}
