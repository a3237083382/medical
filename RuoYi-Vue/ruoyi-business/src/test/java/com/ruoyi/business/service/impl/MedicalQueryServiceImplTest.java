package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
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
import com.ruoyi.business.service.MedicalDataSource;
import com.ruoyi.business.service.MedicalQueryException;

public class MedicalQueryServiceImplTest
{
    private BizInsuranceCompanyMapper companyMapper;
    private BizQueryPriceMapper priceMapper;
    private BizQueryLogMapper queryLogMapper;
    private MedicalDataSource dataSource;
    private BizCompanyQueryPriceMapper companyPriceMapper;
    private BizMonthlyUsageMapper monthlyUsageMapper;
    private BizMedicalQueryRequestMapper workflowRequestMapper;
    private BizMedicalQueryResultMapper workflowResultMapper;
    private MedicalQueryServiceImpl service;

    @BeforeEach
    public void setUp()
    {
        companyMapper = mock(BizInsuranceCompanyMapper.class);
        priceMapper = mock(BizQueryPriceMapper.class);
        queryLogMapper = mock(BizQueryLogMapper.class);
        dataSource = mock(MedicalDataSource.class);
        companyPriceMapper = mock(BizCompanyQueryPriceMapper.class);
        monthlyUsageMapper = mock(BizMonthlyUsageMapper.class);
        workflowRequestMapper = mock(BizMedicalQueryRequestMapper.class);
        workflowResultMapper = mock(BizMedicalQueryResultMapper.class);

        when(companyMapper.selectBizInsuranceCompanyById(1L)).thenReturn(company("100.00", true, "100.00"));
        when(priceMapper.selectBizQueryPriceByQueryType("medical_all")).thenReturn(basePrice("50.00"));
        when(workflowRequestMapper.insertBizMedicalQueryRequest(any())).thenAnswer(invocation -> {
            BizMedicalQueryRequest request = invocation.getArgument(0);
            request.setId(1L);
            return 1;
        });
        when(workflowRequestMapper.markProcessing(1L)).thenReturn(1);
        when(workflowRequestMapper.finishRequest(any(), anyString(), anyString(), anyString(), any(), any())).thenReturn(1);
        when(workflowResultMapper.insertBizMedicalQueryResult(any())).thenReturn(1);
        when(queryLogMapper.insertBizQueryLog(any())).thenAnswer(invocation -> {
            BizQueryLog log = invocation.getArgument(0);
            log.setId(1L);
            return 1;
        });
        when(monthlyUsageMapper.ensureUsage(any(), anyString(), any())).thenReturn(1);
        when(monthlyUsageMapper.reserveBudget(any(), anyString(), any(), any())).thenReturn(1);
        when(monthlyUsageMapper.confirmBudget(any(), anyString(), any(), any())).thenReturn(1);
        when(monthlyUsageMapper.releaseBudget(any(), anyString(), any())).thenReturn(1);
        when(dataSource.sourceCode(any(MedicalQueryRequest.class))).thenReturn("mock");
        when(dataSource.query(any())).thenReturn(hitData());

        service = new MedicalQueryServiceImpl(companyMapper, priceMapper, queryLogMapper, dataSource,
                companyPriceMapper, monthlyUsageMapper, mock(IBizHistoryQueryService.class),
                workflowRequestMapper, workflowResultMapper, null);
    }

    @Test
    public void hitQueryPersistsWorkflowResultAndDesensitizedLog()
    {
        when(companyPriceMapper.selectCompanyPrice(1L, "medical_all"))
                .thenReturn(companyPrice("20.00", "3.00", "0"));

        MedicalQueryResult result = service.query(request());

        assertEquals("COMPLETED", result.getProcessStatus());
        assertEquals("UPLOADED", result.getUploadStatus());
        assertEquals("HIT", result.getResultStatus());
        assertEquals(new BigDecimal("20.00"), result.getFee());
        assertTrue(result.getRequestNo().startsWith("MR"));
        assertTrue(result.getRequestNo().length() <= 32);

        ArgumentCaptor<BizMedicalQueryRequest> requestCaptor = ArgumentCaptor.forClass(BizMedicalQueryRequest.class);
        verify(workflowRequestMapper).insertBizMedicalQueryRequest(requestCaptor.capture());
        BizMedicalQueryRequest workflow = requestCaptor.getValue();
        assertEquals("SINGLE", workflow.getEntryType());
        assertEquals("REALTIME", workflow.getServiceMode());
        assertEquals("张三", workflow.getPatientName());
        assertEquals("430102199001011234", workflow.getIdCard());

        ArgumentCaptor<BizMedicalQueryResult> resultCaptor = ArgumentCaptor.forClass(BizMedicalQueryResult.class);
        verify(workflowResultMapper).insertBizMedicalQueryResult(resultCaptor.capture());
        assertEquals("MOCK", resultCaptor.getValue().getResultSource());
        assertFalse(resultCaptor.getValue().getResultData().contains("430102199001011234"));
        assertTrue(resultCaptor.getValue().getResultData().contains("430***********1234"));

        ArgumentCaptor<BizQueryLog> logCaptor = ArgumentCaptor.forClass(BizQueryLog.class);
        verify(queryLogMapper).insertBizQueryLog(logCaptor.capture());
        BizQueryLog log = logCaptor.getValue();
        assertEquals(result.getRequestNo(), log.getRequestNo());
        assertEquals("REALTIME", log.getServiceMode());
        assertEquals("SINGLE", log.getEntryType());
        assertFalse(log.getQueryParams().contains("430102199001011234"));
        assertTrue(log.getQueryParams().contains("430***********1234"));

        verify(monthlyUsageMapper).confirmBudget(1L, workflow.getBillingMonth(),
                new BigDecimal("20.00"), new BigDecimal("20.00"));
        verify(workflowRequestMapper).finishRequest(1L, "COMPLETED", "UPLOADED", "HIT",
                new BigDecimal("20.00"), 1L);

        InOrder order = inOrder(workflowRequestMapper, dataSource, workflowResultMapper);
        order.verify(workflowRequestMapper).markProcessing(1L);
        order.verify(dataSource).query(any());
        order.verify(workflowResultMapper).insertBizMedicalQueryResult(any());
    }

    @Test
    public void noResultIsFreeEvenWhenLegacyCompanyPriceContainsNoResultFee()
    {
        when(companyPriceMapper.selectCompanyPrice(1L, "medical_all"))
                .thenReturn(companyPrice("20.00", "3.00", "0"));
        when(dataSource.query(any())).thenReturn(new LinkedHashMap<>());

        MedicalQueryResult result = service.query(request());

        assertEquals("NO_RESULT", result.getResultStatus());
        assertEquals(new BigDecimal("0.00"), result.getFee());
        verify(monthlyUsageMapper).confirmBudget(any(), anyString(),
                org.mockito.ArgumentMatchers.eq(new BigDecimal("20.00")),
                org.mockito.ArgumentMatchers.eq(new BigDecimal("0.00")));
        verify(workflowRequestMapper).finishRequest(1L, "COMPLETED", "UPLOADED", "NO_RESULT",
                new BigDecimal("0.00"), 1L);
    }

    @Test
    public void sourceFailureReleasesReservationAndPersistsFailedTerminalState()
    {
        when(companyPriceMapper.selectCompanyPrice(1L, "medical_all"))
                .thenReturn(companyPrice("20.00", "3.00", "0"));
        when(dataSource.query(any())).thenThrow(new MedicalQueryException("5003", "source failed"));

        MedicalQueryException exception = assertThrows(MedicalQueryException.class, () -> service.query(request()));

        assertEquals("5003", exception.getCode());
        verify(monthlyUsageMapper).releaseBudget(any(), anyString(), org.mockito.ArgumentMatchers.eq(new BigDecimal("20.00")));
        verify(workflowResultMapper, never()).insertBizMedicalQueryResult(any());
        verify(workflowRequestMapper).finishRequest(1L, "FAILED", "NOT_UPLOADED", "FAILED",
                new BigDecimal("0.00"), 1L);

        ArgumentCaptor<BizQueryLog> logCaptor = ArgumentCaptor.forClass(BizQueryLog.class);
        verify(queryLogMapper).insertBizQueryLog(logCaptor.capture());
        assertEquals("FAILED", logCaptor.getValue().getResultStatus());
        assertEquals("1", logCaptor.getValue().getStatus());
        assertEquals(new BigDecimal("0.00"), logCaptor.getValue().getFeeSnapshot());
    }

    @Test
    public void budgetReservationFailureStopsBeforeCallingDataSource()
    {
        when(companyPriceMapper.selectCompanyPrice(1L, "medical_all"))
                .thenReturn(companyPrice("20.00", "3.00", "0"));
        when(monthlyUsageMapper.reserveBudget(any(), anyString(), any(), any())).thenReturn(0);

        MedicalQueryException exception = assertThrows(MedicalQueryException.class, () -> service.query(request()));

        assertEquals("4001", exception.getCode());
        verify(dataSource, never()).query(any());
        verify(queryLogMapper, never()).insertBizQueryLog(any());
        verify(workflowRequestMapper, never()).markProcessing(any());
    }

    @Test
    public void disabledCompanyPriceDoesNotFallBackToGlobalPrice()
    {
        when(companyPriceMapper.selectCompanyPrice(1L, "medical_all"))
                .thenReturn(companyPrice("20.00", "3.00", "1"));

        MedicalQueryException exception = assertThrows(MedicalQueryException.class, () -> service.query(request()));

        assertEquals("4003", exception.getCode());
        verify(dataSource, never()).query(any());
        verify(workflowRequestMapper, never()).insertBizMedicalQueryRequest(any());
    }

    @Test
    public void insufficientLegacyBalanceStopsBeforeCreatingRequest()
    {
        when(companyMapper.selectBizInsuranceCompanyById(1L)).thenReturn(company("10.00", false, null));
        when(companyPriceMapper.selectCompanyPrice(1L, "medical_all"))
                .thenReturn(companyPrice("20.00", "3.00", "0"));

        MedicalQueryException exception = assertThrows(MedicalQueryException.class, () -> service.query(request()));

        assertEquals("4001", exception.getCode());
        verify(workflowRequestMapper, never()).insertBizMedicalQueryRequest(any());
        verify(dataSource, never()).query(any());
    }

    @Test
    public void dataSourceCallRunsBetweenStartAndCompletionTransactions()
    {
        when(companyPriceMapper.selectCompanyPrice(1L, "medical_all"))
                .thenReturn(companyPrice("20.00", "3.00", "0"));
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);
        MedicalQueryServiceImpl transactionalService = new MedicalQueryServiceImpl(companyMapper, priceMapper,
                queryLogMapper, dataSource, companyPriceMapper, monthlyUsageMapper,
                mock(IBizHistoryQueryService.class), workflowRequestMapper, workflowResultMapper, transactionManager);

        transactionalService.query(request());

        verify(transactionManager, times(2)).getTransaction(any(TransactionDefinition.class));
        verify(transactionManager, times(2)).commit(transactionStatus);
        InOrder order = inOrder(transactionManager, dataSource);
        order.verify(transactionManager).commit(transactionStatus);
        order.verify(dataSource).query(any());
        order.verify(transactionManager).getTransaction(any(TransactionDefinition.class));
    }

    private MedicalQueryRequest request()
    {
        MedicalQueryRequest request = new MedicalQueryRequest();
        request.setCompanyId(1L);
        request.setQueryType("medical_all");
        request.setRequestIp("127.0.0.1");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", "张三");
        params.put("idCard", "430102199001011234");
        request.setQueryParams(params);
        return request;
    }

    private BizInsuranceCompany company(String balance, boolean budgetEnabled, String budget)
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(1L);
        company.setStatus("0");
        company.setBalance(new BigDecimal(balance));
        company.setBudgetEnabled(budgetEnabled ? "0" : "1");
        company.setMonthlyBudget(budget == null ? null : new BigDecimal(budget));
        return company;
    }

    private BizQueryPrice basePrice(String fee)
    {
        BizQueryPrice price = new BizQueryPrice();
        price.setQueryType("medical_all");
        price.setQueryName("医疗大数据");
        price.setFee(new BigDecimal(fee));
        price.setStatus("0");
        return price;
    }

    private BizCompanyQueryPrice companyPrice(String hitFee, String noResultFee, String status)
    {
        BizCompanyQueryPrice price = new BizCompanyQueryPrice();
        price.setId(9L);
        price.setCompanyId(1L);
        price.setQueryType("medical_all");
        price.setHitFee(new BigDecimal(hitFee));
        price.setNoResultFee(new BigDecimal(noResultFee));
        price.setStatus(status);
        return price;
    }

    private Map<String, Object> hitData()
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("patientName", "张三");
        data.put("idCard", "430102199001011234");
        data.put("diagnosis", "高血压");
        return data;
    }
}
