package com.ruoyi.web.controller.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMonthlyUsage;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.MedicalQueryBatchPreview;
import com.ruoyi.business.domain.medical.MedicalQueryBatchRow;
import com.ruoyi.business.domain.medical.MedicalQueryBatchValidationCommand;
import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmission;
import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmissionResult;
import com.ruoyi.business.domain.medical.MedicalQueryBatchProgress;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.service.IBizQueryPriceService;
import com.ruoyi.business.service.IDelayedMedicalQueryService;
import com.ruoyi.business.service.IMedicalQueryService;
import com.ruoyi.business.service.IMedicalQueryBatchService;
import com.ruoyi.business.service.IMedicalQueryBatchSubmissionService;
import com.ruoyi.business.service.IMedicalQueryBatchCancellationService;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.core.CompanyEmbedRequestContext;

public class CompanyEmbedMedicalQueryControllerTest
{
    private IBizQueryPriceService priceService;
    private BizMonthlyUsageMapper monthlyUsageMapper;
    private BizCompanyQueryPriceMapper companyPriceMapper;
    private IMedicalQueryService medicalQueryService;
    private IDelayedMedicalQueryService delayedMedicalQueryService;
    private IMedicalQueryBatchService medicalQueryBatchService;
    private IMedicalQueryBatchSubmissionService medicalQueryBatchSubmissionService;
    private IMedicalQueryBatchCancellationService medicalQueryBatchCancellationService;
    private CompanyEmbedMedicalQueryController controller;

    @BeforeEach
    public void setUp()
    {
        priceService = mock(IBizQueryPriceService.class);
        monthlyUsageMapper = mock(BizMonthlyUsageMapper.class);
        companyPriceMapper = mock(BizCompanyQueryPriceMapper.class);
        medicalQueryService = mock(IMedicalQueryService.class);
        delayedMedicalQueryService = mock(IDelayedMedicalQueryService.class);
        medicalQueryBatchService = mock(IMedicalQueryBatchService.class);
        medicalQueryBatchSubmissionService = mock(IMedicalQueryBatchSubmissionService.class);
        medicalQueryBatchCancellationService = mock(IMedicalQueryBatchCancellationService.class);
        controller = new CompanyEmbedMedicalQueryController(priceService, medicalQueryService,
                delayedMedicalQueryService, medicalQueryBatchService, monthlyUsageMapper, companyPriceMapper,
                medicalQueryBatchSubmissionService, medicalQueryBatchCancellationService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void queryTypesUsesCompanyPriceAndExcludesDisabledAndDelayedItems()
    {
        BizQueryPrice active = basePrice("medical_all", "医疗大数据", "40.00", "0");
        BizQueryPrice disabledForCompany = basePrice("clinic", "门诊记录", "20.00", "0");
        BizQueryPrice delayed = basePrice("precision_delayed", "精准延时", "0.00", "0");
        when(priceService.selectBizQueryPriceList(any(BizQueryPrice.class)))
                .thenReturn(Arrays.asList(active, disabledForCompany, delayed));
        when(companyPriceMapper.selectCompanyPrice(1L, "medical_all"))
                .thenReturn(companyPrice("企业医疗查询", "55.00", "0"));
        when(companyPriceMapper.selectCompanyPrice(1L, "clinic"))
                .thenReturn(companyPrice("门诊记录", "20.00", "1"));

        AjaxResult result = controller.queryTypes(request(company(true, "100.00")));
        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");

        assertEquals(1, data.size());
        assertEquals("medical_all", data.get(0).get("queryType"));
        assertEquals("企业医疗查询", data.get(0).get("queryName"));
        assertEquals(new BigDecimal("55.00"), data.get(0).get("hitFee"));
        assertEquals(new BigDecimal("0.00"), data.get(0).get("noResultFee"));
        assertNull(data.get(0).get("id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void accessReturnsMaskedCompanyAndCapabilityStatus()
    {
        BizInsuranceCompany company = company(true, "100.00");
        BizQueryPrice realtime = basePrice("medical_all", "医疗大数据", "40.00", "0");
        BizQueryPrice delayed = basePrice("precision_delayed", "精准延时", "0.00", "1");
        when(priceService.selectBizQueryPriceList(any(BizQueryPrice.class))).thenReturn(List.of(realtime));
        when(priceService.selectBizQueryPriceByQueryType("precision_delayed")).thenReturn(delayed);

        AjaxResult result = controller.access(request(company));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> capabilities = (Map<String, Object>) data.get("capabilities");

        assertEquals("测试保险公司", data.get("companyName"));
        assertEquals("TEST", data.get("companyCode"));
        assertEquals("1234****cdef", data.get("appKeyMasked"));
        assertNull(data.get("id"));
        assertNull(data.get("appKey"));
        assertEquals("NORMAL", data.get("serviceStatus"));
        assertTrue((Boolean) capabilities.get("singleRealtime"));
        assertFalse((Boolean) capabilities.get("singleDelayed"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void usageMarksNearLimitFromUsedAndReservedAmount()
    {
        BizInsuranceCompany company = company(true, "100.00");
        BizMonthlyUsage usage = new BizMonthlyUsage();
        usage.setUsedAmount(new BigDecimal("70.00"));
        usage.setReservedAmount(new BigDecimal("15.00"));
        usage.setStatus("0");
        when(monthlyUsageMapper.selectUsage(1L, java.time.YearMonth.now().toString())).thenReturn(usage);

        AjaxResult result = controller.usage(request(company));
        Map<String, Object> data = (Map<String, Object>) result.get("data");

        assertEquals(new BigDecimal("15.00"), data.get("remaining"));
        assertEquals(85, data.get("usagePercent"));
        assertEquals("NEAR_LIMIT", data.get("serviceStatus"));
        assertTrue((Boolean) data.get("budgetEnabled"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void usageStaysNormalWhenBudgetControlIsDisabled()
    {
        BizInsuranceCompany company = company(false, "0.00");

        AjaxResult result = controller.usage(request(company));
        Map<String, Object> data = (Map<String, Object>) result.get("data");

        assertEquals(0, data.get("usagePercent"));
        assertEquals("NORMAL", data.get("serviceStatus"));
        assertFalse((Boolean) data.get("budgetEnabled"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void usageMarksEnabledZeroBudgetAsLimitReached()
    {
        BizInsuranceCompany company = company(true, "0.00");

        AjaxResult result = controller.usage(request(company));
        Map<String, Object> data = (Map<String, Object>) result.get("data");

        assertEquals(100, data.get("usagePercent"));
        assertEquals("LIMIT_REACHED", data.get("serviceStatus"));
        assertTrue((Boolean) data.get("budgetEnabled"));
    }

    @Test
    public void queryMapsSourceFailureToStableErrorCode()
    {
        when(medicalQueryService.query(any())).thenThrow(new MedicalQueryException("5003", "source failed"));
        Map<String, Object> queryParams = new java.util.LinkedHashMap<>();
        queryParams.put("name", "张三");
        queryParams.put("idCard", "430102199001011234");
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("queryType", "medical_all");
        body.put("queryParams", queryParams);

        AjaxResult result = controller.query(body, request(company(true, "100.00")));

        assertEquals(503, result.get("code"));
        assertEquals("SOURCE_UNAVAILABLE", result.get("errorCode"));
    }

    @Test
    public void delayedSubmissionBindsAuthenticatedCompany()
    {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("name", "张三");
        body.put("idCard", "430102199001011234");

        AjaxResult result = controller.submitDelayed(body, request(company(true, "100.00")));

        assertEquals(200, result.get("code"));
        verify(delayedMedicalQueryService).submit(eq(1L), eq("张三"), eq("430102199001011234"), any());
    }

    @Test
    public void editedBatchRowsArePassedToValidationService()
    {
        MedicalQueryBatchRow row = new MedicalQueryBatchRow();
        row.setRowNo(2);
        row.setName("张三");
        row.setIdCard("430102199001011234");
        MedicalQueryBatchValidationCommand command = new MedicalQueryBatchValidationCommand();
        command.setRows(List.of(row));
        when(medicalQueryBatchService.validate(command.getRows())).thenReturn(new MedicalQueryBatchPreview());

        AjaxResult result = controller.validateBatch(command, request(company(true, "100.00")));

        assertEquals(200, result.get("code"));
        verify(medicalQueryBatchService).validate(command.getRows());
    }

    @Test
    public void batchPreviewMapsInvalidFileTypeToStableErrorCode()
    {
        MockMultipartFile file = new MockMultipartFile("file", "名单.csv", "text/csv", new byte[] { 1 });
        when(medicalQueryBatchService.preview(file))
                .thenThrow(new MedicalQueryException("4004", "只支持.xlsx或.xls文件"));

        AjaxResult result = controller.importBatchPreview(file, request(company(true, "100.00")));

        assertEquals(400, result.get("code"));
        assertEquals("INVALID_FILE_TYPE", result.get("errorCode"));
    }

    @Test
    public void batchSubmissionBindsAuthenticatedCompany()
    {
        MedicalQueryBatchSubmission command = new MedicalQueryBatchSubmission();
        command.setServiceMode("DELAYED");
        command.setRows(List.of());
        when(medicalQueryBatchSubmissionService.submit(eq(1L), eq(command), any()))
                .thenReturn(new MedicalQueryBatchSubmissionResult());

        AjaxResult result = controller.submitBatch(command, request(company(true, "100.00")));

        assertEquals(200, result.get("code"));
        verify(medicalQueryBatchSubmissionService).submit(eq(1L), eq(command), any());
    }

    @Test
    public void batchProgressIsScopedToAuthenticatedCompany()
    {
        when(medicalQueryBatchCancellationService.getProgress(1L, "B001"))
                .thenReturn(new MedicalQueryBatchProgress());

        AjaxResult result = controller.batchProgress("B001", request(company(true, "100.00")));

        assertEquals(200, result.get("code"));
        verify(medicalQueryBatchCancellationService).getProgress(1L, "B001");
    }

    @Test
    public void startedBatchItemMapsToNotCancellable()
    {
        when(medicalQueryBatchCancellationService.cancelItem(1L, 11L))
                .thenThrow(new MedicalQueryException("4092", "item cannot be cancelled"));

        AjaxResult result = controller.cancelBatchItem(11L, request(company(true, "100.00")));

        assertEquals(409, result.get("code"));
        assertEquals("NOT_CANCELLABLE", result.get("errorCode"));
    }

    @Test
    public void requestDetailMapsMissingRequestToStableErrorCode()
    {
        when(delayedMedicalQueryService.getRequest(1L, "MD404"))
                .thenThrow(new MedicalQueryException("4041", "request not found"));

        AjaxResult result = controller.requestDetail("MD404", request(company(true, "100.00")));

        assertEquals(404, result.get("code"));
        assertEquals("REQUEST_NOT_FOUND", result.get("errorCode"));
    }

    @Test
    public void requestHistoryUsesAppKeyCompanyScope()
    {
        when(delayedMedicalQueryService.listHistory(1L, null, null, null, null, null, null))
                .thenReturn(List.of());

        AjaxResult result = controller.requestHistory(null, null, null, null, null, null,
                request(company(true, "100.00")));

        assertEquals(200, result.get("code"));
        verify(delayedMedicalQueryService).listHistory(1L, null, null, null, null, null, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void unreadNotificationReturnsCompanyScopedCount()
    {
        when(delayedMedicalQueryService.countUnread(1L)).thenReturn(4);

        AjaxResult result = controller.unreadNotifications(request(company(true, "100.00")));
        Map<String, Object> data = (Map<String, Object>) result.get("data");

        assertEquals(4, data.get("unreadCount"));
    }

    @Test
    public void markReadMapsResultNotReadyError()
    {
        org.mockito.Mockito.doThrow(new MedicalQueryException("4093", "result is not ready"))
                .when(delayedMedicalQueryService).markRead(1L, "MD_PENDING");

        AjaxResult result = controller.markRequestRead("MD_PENDING", request(company(true, "100.00")));

        assertEquals(409, result.get("code"));
        assertEquals("RESULT_NOT_READY", result.get("errorCode"));
    }

    private MockHttpServletRequest request(BizInsuranceCompany company)
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CompanyEmbedRequestContext.COMPANY_ATTRIBUTE, company);
        return request;
    }

    private BizInsuranceCompany company(boolean budgetEnabled, String budget)
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(1L);
        company.setCompanyName("测试保险公司");
        company.setCompanyCode("TEST");
        company.setAppKey("1234567890abcdef");
        company.setBudgetEnabled(budgetEnabled ? "0" : "1");
        company.setMonthlyBudget(new BigDecimal(budget));
        return company;
    }

    private BizQueryPrice basePrice(String queryType, String queryName, String fee, String status)
    {
        BizQueryPrice price = new BizQueryPrice();
        price.setQueryType(queryType);
        price.setQueryName(queryName);
        price.setFee(new BigDecimal(fee));
        price.setStatus(status);
        return price;
    }

    private BizCompanyQueryPrice companyPrice(String queryName, String hitFee, String status)
    {
        BizCompanyQueryPrice price = new BizCompanyQueryPrice();
        price.setQueryName(queryName);
        price.setHitFee(new BigDecimal(hitFee));
        price.setStatus(status);
        return price;
    }
}
