package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizMedicalQueryResult;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.DelayedMedicalQuerySubmission;
import com.ruoyi.business.domain.medical.MedicalQueryRequestDetail;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMedicalQueryRequestMapper;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMedicalQueryResultMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.mapper.BizQueryPriceMapper;
import com.ruoyi.business.service.MedicalQueryException;

public class DelayedMedicalQueryServiceImplTest
{
    private BizInsuranceCompanyMapper companyMapper;
    private BizQueryPriceMapper priceMapper;
    private BizCompanyQueryPriceMapper companyPriceMapper;
    private BizMonthlyUsageMapper monthlyUsageMapper;
    private BizMedicalQueryRequestMapper requestMapper;
    private BizMedicalQueryBatchMapper batchMapper;
    private BizMedicalQueryResultMapper resultMapper;
    private DelayedMedicalQueryServiceImpl service;

    @BeforeEach
    public void setUp()
    {
        companyMapper = mock(BizInsuranceCompanyMapper.class);
        priceMapper = mock(BizQueryPriceMapper.class);
        companyPriceMapper = mock(BizCompanyQueryPriceMapper.class);
        monthlyUsageMapper = mock(BizMonthlyUsageMapper.class);
        requestMapper = mock(BizMedicalQueryRequestMapper.class);
        batchMapper = mock(BizMedicalQueryBatchMapper.class);
        resultMapper = mock(BizMedicalQueryResultMapper.class);

        when(companyMapper.selectBizInsuranceCompanyByIdForUpdate(1L)).thenReturn(company());
        when(priceMapper.selectBizQueryPriceByQueryType("precision_delayed")).thenReturn(basePrice());
        when(companyPriceMapper.selectCompanyPrice(1L, "precision_delayed")).thenReturn(companyPrice());
        when(requestMapper.insertBizMedicalQueryRequest(any())).thenAnswer(invocation -> {
            BizMedicalQueryRequest request = invocation.getArgument(0);
            request.setId(10L);
            return 1;
        });
        when(monthlyUsageMapper.ensureUsage(any(), anyString(), any())).thenReturn(1);
        when(monthlyUsageMapper.reserveBudget(any(), anyString(), any(), any())).thenReturn(1);

        service = new DelayedMedicalQueryServiceImpl(companyMapper, priceMapper, companyPriceMapper,
                monthlyUsageMapper, requestMapper, batchMapper, resultMapper, null);
    }

    @Test
    public void newSubmissionCreatesPendingRequestAndReservesHitFee()
    {
        DelayedMedicalQuerySubmission submission = service.submit(1L, " 张三 ", "43010219900101123x", "127.0.0.1");

        assertFalse(submission.isReused());
        assertTrue(submission.getRequestNo().startsWith("MD"));
        assertEquals("PENDING", submission.getProcessStatus());
        assertEquals("NOT_UPLOADED", submission.getUploadStatus());
        assertNull(submission.getResultStatus());

        ArgumentCaptor<BizMedicalQueryRequest> captor = ArgumentCaptor.forClass(BizMedicalQueryRequest.class);
        verify(requestMapper).insertBizMedicalQueryRequest(captor.capture());
        BizMedicalQueryRequest request = captor.getValue();
        assertEquals("张三", request.getPatientName());
        assertEquals("43010219900101123X", request.getIdCard());
        assertEquals("SINGLE", request.getEntryType());
        assertEquals("DELAYED", request.getServiceMode());
        assertNull(request.getQueryType());
        assertEquals(new BigDecimal("20.00"), request.getReservedFee());
        assertEquals(9L, request.getPriceConfigId());
        verify(monthlyUsageMapper).reserveBudget(1L, request.getBillingMonth(), new BigDecimal("100.00"),
                new BigDecimal("20.00"));

        InOrder order = inOrder(companyMapper, requestMapper);
        order.verify(companyMapper).selectBizInsuranceCompanyByIdForUpdate(1L);
        order.verify(requestMapper).selectReusableDelayedRequest(1L, "张三", "43010219900101123X");
        order.verify(requestMapper).insertBizMedicalQueryRequest(any());
    }

    @Test
    public void unfinishedSubmissionIsReusedWithoutSecondReservation()
    {
        BizMedicalQueryRequest existing = delayedRequest("MD_EXISTING", "PENDING", "NOT_UPLOADED");
        when(requestMapper.selectReusableDelayedRequest(1L, "张三", "430102199001011234"))
                .thenReturn(existing);

        DelayedMedicalQuerySubmission submission = service.submit(1L, "张三", "430102199001011234", "127.0.0.1");

        assertTrue(submission.isReused());
        assertEquals("MD_EXISTING", submission.getRequestNo());
        verify(requestMapper, never()).insertBizMedicalQueryRequest(any());
        verify(monthlyUsageMapper, never()).reserveBudget(any(), anyString(), any(), any());
        verify(priceMapper, never()).selectBizQueryPriceByQueryType(anyString());
    }

    @Test
    public void invalidIdentityIsRejectedBeforeDatabaseAccess()
    {
        MedicalQueryException exception = assertThrows(MedicalQueryException.class,
                () -> service.submit(1L, "张三", "123", "127.0.0.1"));

        assertEquals("4000", exception.getCode());
        verify(companyMapper, never()).selectBizInsuranceCompanyByIdForUpdate(any());
    }

    @Test
    public void pendingDetailDoesNotLoadDraftResult()
    {
        BizMedicalQueryRequest request = delayedRequest("MD_PENDING", "PROCESSING", "NOT_UPLOADED");
        when(requestMapper.selectCompanyRequestByNo(1L, "MD_PENDING")).thenReturn(request);

        MedicalQueryRequestDetail detail = service.getRequest(1L, "MD_PENDING");

        assertFalse(detail.isResultVisible());
        assertNull(detail.getData());
        assertEquals("张*", detail.getPatientName());
        assertEquals("430***********1234", detail.getIdCard());
        verify(resultMapper, never()).selectByRequestId(any());
    }

    @Test
    public void uploadedDetailReturnsDesensitizedDynamicResult()
    {
        BizMedicalQueryRequest request = delayedRequest("MD_DONE", "COMPLETED", "UPLOADED");
        request.setResultStatus("HIT");
        request.setFeeSnapshot(new BigDecimal("20.00"));
        BizMedicalQueryResult stored = new BizMedicalQueryResult();
        stored.setRequestId(request.getId());
        stored.setColumnSchema("[{\"field\":\"idCard\",\"label\":\"身份证号\"}]");
        stored.setResultData("{\"patientName\":\"张三\",\"idCard\":\"430102199001011234\",\"diagnosis\":\"高血压\"}");
        stored.setResultSummary("查询完成");
        when(requestMapper.selectCompanyRequestByNo(1L, "MD_DONE")).thenReturn(request);
        when(resultMapper.selectByRequestId(request.getId())).thenReturn(stored);

        MedicalQueryRequestDetail detail = service.getRequest(1L, "MD_DONE");

        assertTrue(detail.isResultVisible());
        assertEquals("张*", detail.getData().get("patientName"));
        assertEquals("430***********1234", detail.getData().get("idCard"));
        assertEquals("高血***", detail.getData().get("diagnosis"));
        assertEquals("查询完成", detail.getResultSummary());
        assertEquals(new BigDecimal("20.00"), detail.getFee());
    }

    @Test
    public void requestLookupIsAlwaysScopedToCompany()
    {
        MedicalQueryException exception = assertThrows(MedicalQueryException.class,
                () -> service.getRequest(2L, "MD_OTHER"));

        assertEquals("4041", exception.getCode());
        verify(requestMapper).selectCompanyRequestByNo(2L, "MD_OTHER");
    }

    private BizInsuranceCompany company()
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(1L);
        company.setStatus("0");
        company.setBalance(new BigDecimal("100.00"));
        company.setBudgetEnabled("0");
        company.setMonthlyBudget(new BigDecimal("100.00"));
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
        price.setCompanyId(1L);
        price.setQueryType("precision_delayed");
        price.setHitFee(new BigDecimal("20.00"));
        price.setStatus("0");
        return price;
    }

    @Test
    public void historyOnlyReturnsCompanyDelayedRequestsAndHidesDraftResult()
    {
        BizMedicalQueryRequest completed = delayedRequest("MD_HISTORY", "COMPLETED", "UPLOADED");
        when(requestMapper.selectCompanyDelayedHistory(any())).thenReturn(java.util.List.of(completed));

        var history = service.listHistory(1L, null, null, null, null, null, null);

        assertEquals(1, history.size());
        assertEquals("MD_HISTORY", history.get(0).getRequestNo());
        assertFalse(history.get(0).isResultVisible());
        verify(resultMapper, never()).selectByRequestId(10L);
    }

    @Test
    public void unreadCountIsCompanyScoped()
    {
        when(requestMapper.countCompanyUnreadDelayedResults(1L)).thenReturn(3);

        assertEquals(3, service.countUnread(1L));
        verify(requestMapper).countCompanyUnreadDelayedResults(1L);
    }

    @Test
    public void markReadRejectsResultThatHasNotBeenUploaded()
    {
        when(requestMapper.selectCompanyRequestByNo(1L, "MD_PENDING"))
                .thenReturn(delayedRequest("MD_PENDING", "PROCESSING", "NOT_UPLOADED"));

        MedicalQueryException exception = assertThrows(MedicalQueryException.class,
                () -> service.markRead(1L, "MD_PENDING"));

        assertEquals("4093", exception.getCode());
        verify(requestMapper, never()).markCompanyDelayedRequestRead(any(), anyString());
    }

    @Test
    public void markReadIsIdempotentForAlreadyReadUploadedResult()
    {
        BizMedicalQueryRequest request = delayedRequest("MD_READ", "COMPLETED", "UPLOADED");
        request.setViewStatus("READ");
        when(requestMapper.selectCompanyRequestByNo(1L, "MD_READ")).thenReturn(request);

        service.markRead(1L, "MD_READ");

        verify(requestMapper, never()).markCompanyDelayedRequestRead(any(), anyString());
    }

    private BizMedicalQueryRequest delayedRequest(String requestNo, String processStatus, String uploadStatus)
    {
        BizMedicalQueryRequest request = new BizMedicalQueryRequest();
        request.setId(10L);
        request.setRequestNo(requestNo);
        request.setCompanyId(1L);
        request.setEntryType("SINGLE");
        request.setServiceMode("DELAYED");
        request.setPatientName("张三");
        request.setIdCard("430102199001011234");
        request.setProcessStatus(processStatus);
        request.setUploadStatus(uploadStatus);
        request.setViewStatus("READ");
        request.setFeeSnapshot(BigDecimal.ZERO.setScale(2));
        request.setCreateTime(new Date());
        return request;
    }
}
