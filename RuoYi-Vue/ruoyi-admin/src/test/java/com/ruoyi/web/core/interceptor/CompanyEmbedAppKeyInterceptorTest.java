package com.ruoyi.web.core.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.web.core.CompanyEmbedRequestContext;

public class CompanyEmbedAppKeyInterceptorTest
{
    private IBizInsuranceCompanyService companyService;
    private CompanyEmbedAppKeyInterceptor interceptor;

    @BeforeEach
    public void setUp()
    {
        companyService = mock(IBizInsuranceCompanyService.class);
        interceptor = new CompanyEmbedAppKeyInterceptor(companyService);
    }

    @Test
    public void rejectsMissingAppKey() throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/company/embed/medical/usage");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        assertEquals("INVALID_APP_KEY", JSON.parseObject(response.getContentAsString()).getString("errorCode"));
        verify(companyService, never()).selectBizInsuranceCompanyByAppKey(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void rejectsUnknownAppKey() throws Exception
    {
        when(companyService.selectBizInsuranceCompanyByAppKey("unknown")).thenReturn(null);
        MockHttpServletRequest request = requestWithAppKey(" unknown ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
        assertEquals("INVALID_APP_KEY", JSON.parseObject(response.getContentAsString()).getString("errorCode"));
        verify(companyService).selectBizInsuranceCompanyByAppKey("unknown");
    }

    @Test
    public void rejectsDisabledCompany() throws Exception
    {
        BizInsuranceCompany company = company("1");
        when(companyService.selectBizInsuranceCompanyByAppKey("disabled")).thenReturn(company);
        MockHttpServletRequest request = requestWithAppKey("disabled");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
        assertEquals("COMPANY_DISABLED", JSON.parseObject(response.getContentAsString()).getString("errorCode"));
        assertNull(CompanyEmbedRequestContext.getCompany(request));
    }

    @Test
    public void storesActiveCompanyInRequestContext() throws Exception
    {
        BizInsuranceCompany company = company("0");
        when(companyService.selectBizInsuranceCompanyByAppKey("active")).thenReturn(company);
        MockHttpServletRequest request = requestWithAppKey("active");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertSame(company, CompanyEmbedRequestContext.getCompany(request));
    }

    @Test
    public void allowsCorsPreflightWithoutAppKey() throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/company/embed/medical/query");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        verify(companyService, never()).selectBizInsuranceCompanyByAppKey(org.mockito.ArgumentMatchers.anyString());
    }

    private MockHttpServletRequest requestWithAppKey(String appKey)
    {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/company/embed/medical/usage");
        request.addHeader(CompanyEmbedAppKeyInterceptor.APP_KEY_HEADER, appKey);
        return request;
    }

    private BizInsuranceCompany company(String status)
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(1L);
        company.setStatus(status);
        return company;
    }
}
