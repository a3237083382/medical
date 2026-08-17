package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.BizMedicalQueryBatchItem;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.MedicalQueryBatchRow;
import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmission;
import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmissionResult;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMedicalQueryRequestMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.mapper.BizQueryPriceMapper;
import com.ruoyi.business.service.MedicalQueryException;

public class MedicalQueryBatchSubmissionServiceImplTest
{
    private BizInsuranceCompanyMapper companyMapper;
    private BizQueryPriceMapper priceMapper;
    private BizCompanyQueryPriceMapper companyPriceMapper;
    private BizMonthlyUsageMapper monthlyUsageMapper;
    private BizMedicalQueryRequestMapper requestMapper;
    private BizMedicalQueryBatchMapper batchMapper;
    private MedicalQueryBatchSubmissionServiceImpl service;

    @BeforeEach
    public void setUp()
    {
        companyMapper = mock(BizInsuranceCompanyMapper.class);
        priceMapper = mock(BizQueryPriceMapper.class);
        companyPriceMapper = mock(BizCompanyQueryPriceMapper.class);
        monthlyUsageMapper = mock(BizMonthlyUsageMapper.class);
        requestMapper = mock(BizMedicalQueryRequestMapper.class);
        batchMapper = mock(BizMedicalQueryBatchMapper.class);

        when(companyMapper.selectBizInsuranceCompanyByIdForUpdate(1L)).thenReturn(company(true, "100.00", "100.00"));
        when(priceMapper.selectBizQueryPriceByQueryType("precision_delayed")).thenReturn(basePrice());
        when(companyPriceMapper.selectCompanyPrice(1L, "precision_delayed")).thenReturn(companyPrice());
        when(monthlyUsageMapper.ensureUsage(any(), anyString(), any())).thenReturn(1);
        when(monthlyUsageMapper.reserveBudget(any(), anyString(), any(), any())).thenReturn(1);
        AtomicLong requestIds = new AtomicLong(10);
        when(requestMapper.insertBizMedicalQueryRequest(any())).thenAnswer(invocation -> {
            BizMedicalQueryRequest request = invocation.getArgument(0);
            request.setId(requestIds.getAndIncrement());
            return 1;
        });
        when(batchMapper.insertBizMedicalQueryBatch(any())).thenAnswer(invocation -> {
            BizMedicalQueryBatch batch = invocation.getArgument(0);
            batch.setId(100L);
            return 1;
        });
        when(batchMapper.insertBizMedicalQueryBatchItem(any())).thenReturn(1);

        service = new MedicalQueryBatchSubmissionServiceImpl(companyMapper, priceMapper, companyPriceMapper,
                monthlyUsageMapper, requestMapper, batchMapper, new MedicalQueryBatchServiceImpl(), null);
    }

    @Test
    public void mixedBatchReusesExistingRequestAndReservesOnlyNewRequest()
    {
        BizMedicalQueryRequest existing = request(9L, "MD_EXISTING", "PROCESSING");
        when(requestMapper.selectReusableDelayedRequest(1L, "张三", "430102199001011234"))
                .thenReturn(existing);

        MedicalQueryBatchSubmissionResult result = service.submit(1L, command(
                row(2, "张三", "430102199001011234"),
                row(3, "李四", "430102199001011235")), "127.0.0.1");

        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getPendingCount());
        assertEquals(1, result.getProcessingCount());
        assertEquals(1, result.getReusedCount());
        assertTrue(result.getBatchNo().startsWith("B"));
        verify(requestMapper, times(1)).insertBizMedicalQueryRequest(any());
        verify(monthlyUsageMapper).reserveBudget(any(), anyString(), any(),
                org.mockito.ArgumentMatchers.eq(new BigDecimal("20.00")));

        ArgumentCaptor<BizMedicalQueryBatchItem> itemCaptor = ArgumentCaptor.forClass(BizMedicalQueryBatchItem.class);
        verify(batchMapper, times(2)).insertBizMedicalQueryBatchItem(itemCaptor.capture());
        assertEquals("1", itemCaptor.getAllValues().get(0).getReusedFlag());
        assertEquals(9L, itemCaptor.getAllValues().get(0).getRequestId());
        assertEquals("0", itemCaptor.getAllValues().get(1).getReusedFlag());

        ArgumentCaptor<BizMedicalQueryRequest> requestCaptor = ArgumentCaptor.forClass(BizMedicalQueryRequest.class);
        verify(requestMapper).insertBizMedicalQueryRequest(requestCaptor.capture());
        assertEquals("BATCH", requestCaptor.getValue().getEntryType());
        assertEquals("DELAYED", requestCaptor.getValue().getServiceMode());
        assertEquals(new BigDecimal("20.00"), requestCaptor.getValue().getReservedFee());
    }

    @Test
    public void allReusedBatchDoesNotRequireCurrentPriceOrReserveAgain()
    {
        BizMedicalQueryRequest existing = request(9L, "MD_EXISTING", "PROCESSING");
        when(requestMapper.selectReusableDelayedRequest(1L, "张三", "430102199001011234"))
                .thenReturn(existing);
        when(priceMapper.selectBizQueryPriceByQueryType("precision_delayed")).thenReturn(null);

        MedicalQueryBatchSubmissionResult result = service.submit(1L,
                command(row(2, "张三", "430102199001011234")), "127.0.0.1");

        assertEquals("PROCESSING", result.getBatchStatus());
        assertEquals(1, result.getReusedCount());
        verify(priceMapper, never()).selectBizQueryPriceByQueryType(anyString());
        verify(monthlyUsageMapper, never()).reserveBudget(any(), anyString(), any(), any());
        verify(requestMapper, never()).insertBizMedicalQueryRequest(any());
    }

    @Test
    public void invalidRowsAreRejectedBeforeDatabaseAccess()
    {
        MedicalQueryException exception = assertThrows(MedicalQueryException.class,
                () -> service.submit(1L, command(row(2, "张三", "123")), "127.0.0.1"));

        assertEquals("4006", exception.getCode());
        verify(companyMapper, never()).selectBizInsuranceCompanyByIdForUpdate(any());
    }

    @Test
    public void aggregateBalanceIsCheckedBeforeAnyRequestIsInserted()
    {
        when(companyMapper.selectBizInsuranceCompanyByIdForUpdate(1L))
                .thenReturn(company(false, null, "30.00"));

        MedicalQueryException exception = assertThrows(MedicalQueryException.class, () -> service.submit(1L,
                command(row(2, "张三", "430102199001011234"), row(3, "李四", "430102199001011235")),
                "127.0.0.1"));

        assertEquals("4001", exception.getCode());
        verify(requestMapper, never()).insertBizMedicalQueryRequest(any());
        verify(batchMapper, never()).insertBizMedicalQueryBatch(any());
    }

    @Test
    public void duplicateRowNumbersAreRejected()
    {
        MedicalQueryException exception = assertThrows(MedicalQueryException.class, () -> service.submit(1L,
                command(row(2, "张三", "430102199001011234"), row(2, "李四", "430102199001011235")),
                "127.0.0.1"));

        assertEquals("4000", exception.getCode());
        verify(companyMapper, never()).selectBizInsuranceCompanyByIdForUpdate(any());
    }

    private MedicalQueryBatchSubmission command(MedicalQueryBatchRow... rows)
    {
        MedicalQueryBatchSubmission command = new MedicalQueryBatchSubmission();
        command.setServiceMode("DELAYED");
        command.setRows(List.of(rows));
        return command;
    }

    private MedicalQueryBatchRow row(int rowNo, String name, String idCard)
    {
        MedicalQueryBatchRow row = new MedicalQueryBatchRow();
        row.setRowNo(rowNo);
        row.setName(name);
        row.setIdCard(idCard);
        return row;
    }

    private BizMedicalQueryRequest request(Long id, String requestNo, String processStatus)
    {
        BizMedicalQueryRequest request = new BizMedicalQueryRequest();
        request.setId(id);
        request.setRequestNo(requestNo);
        request.setCompanyId(1L);
        request.setProcessStatus(processStatus);
        request.setUploadStatus("NOT_UPLOADED");
        return request;
    }

    private BizInsuranceCompany company(boolean budgetEnabled, String monthlyBudget, String balance)
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(1L);
        company.setStatus("0");
        company.setBudgetEnabled(budgetEnabled ? "0" : "1");
        company.setMonthlyBudget(monthlyBudget == null ? null : new BigDecimal(monthlyBudget));
        company.setBalance(new BigDecimal(balance));
        return company;
    }

    private BizQueryPrice basePrice()
    {
        BizQueryPrice price = new BizQueryPrice();
        price.setQueryType("precision_delayed");
        price.setFee(new BigDecimal("30.00"));
        price.setStatus("0");
        return price;
    }

    private BizCompanyQueryPrice companyPrice()
    {
        BizCompanyQueryPrice price = new BizCompanyQueryPrice();
        price.setId(9L);
        price.setQueryType("precision_delayed");
        price.setHitFee(new BigDecimal("20.00"));
        price.setStatus("0");
        return price;
    }
}
