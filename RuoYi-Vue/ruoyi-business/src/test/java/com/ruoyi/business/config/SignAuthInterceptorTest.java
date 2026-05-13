package com.ruoyi.business.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.business.util.SignUtil;
import com.ruoyi.common.core.redis.RedisCache;

class SignAuthInterceptorTest
{
    private BizInsuranceCompany company;
    private SignAuthInterceptor interceptor;

    @BeforeEach
    void setUp()
    {
        company = new BizInsuranceCompany();
        company.setId(9L);
        company.setCompanyName("Test Company");
        company.setAppKey("app-key");
        company.setAppSecret("app-secret");
        company.setBalance(BigDecimal.TEN);
        company.setStatus("0");

        interceptor = new SignAuthInterceptor(new FakeCompanyService(company), new FakeRedisCache());
    }

    @Test
    void preHandleRejectsMissingSignatureHeaders() throws Exception
    {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(new MockHttpServletRequest("GET", "/api/v1/test"), response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleAcceptsValidSignatureAndStoresCompanyContext() throws Exception
    {
        MockHttpServletRequest request = signedRequest("nonce-ok", System.currentTimeMillis(), "{\"q\":\"ok\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals(company.getId(), request.getAttribute("companyId"));
        assertEquals(company.getCompanyName(), request.getAttribute("companyName"));
        assertSame(company, request.getAttribute("company"));
    }

    @Test
    void preHandleRejectsReusedNonce() throws Exception
    {
        MockHttpServletRequest first = signedRequest("nonce-reused", System.currentTimeMillis(), "{}");
        MockHttpServletRequest second = signedRequest("nonce-reused", System.currentTimeMillis(), "{}");

        assertTrue(interceptor.preHandle(first, new MockHttpServletResponse(), new Object()));

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(second, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleRejectsExpiredTimestamp() throws Exception
    {
        MockHttpServletRequest request = signedRequest("nonce-expired", System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(6), "{}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandleRejectsDisabledCompany() throws Exception
    {
        company.setStatus("1");
        MockHttpServletRequest request = signedRequest("nonce-disabled", System.currentTimeMillis(), "{}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    private MockHttpServletRequest signedRequest(String nonce, long timestamp, String body)
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        request.addHeader("X-App-Key", company.getAppKey());
        request.addHeader("X-Timestamp", Long.toString(timestamp));
        request.addHeader("X-Nonce", nonce);
        request.addHeader("X-Sign", SignUtil.sign(Long.toString(timestamp), nonce, body, company.getAppSecret()));
        return request;
    }

    private static class FakeCompanyService implements IBizInsuranceCompanyService
    {
        private final BizInsuranceCompany company;

        FakeCompanyService(BizInsuranceCompany company)
        {
            this.company = company;
        }

        @Override
        public BizInsuranceCompany selectBizInsuranceCompanyByAppKey(String appKey)
        {
            return company.getAppKey().equals(appKey) ? company : null;
        }

        @Override public BizInsuranceCompany selectBizInsuranceCompanyById(Long id) { return null; }
        @Override public List<BizInsuranceCompany> selectBizInsuranceCompanyList(BizInsuranceCompany company) { return List.of(); }
        @Override public int insertBizInsuranceCompany(BizInsuranceCompany company) { return 0; }
        @Override public int updateBizInsuranceCompany(BizInsuranceCompany company) { return 0; }
        @Override public int deleteBizInsuranceCompanyByIds(Long[] ids) { return 0; }
        @Override public BizInsuranceCompany selectBizInsuranceCompanyByUsername(String username) { return null; }
        @Override public int updateLoginInfo(BizInsuranceCompany company) { return 0; }
        @Override public int changeStatus(Long id, String status) { return 0; }
        @Override public int updatePassword(Long id, String password) { return 0; }
    }

    private static class FakeRedisCache extends RedisCache
    {
        private final Set<String> keys = new HashSet<>();

        @Override
        public Boolean hasKey(String key)
        {
            return keys.contains(key);
        }

        @Override
        public <T> void setCacheObject(String key, T value, Integer timeout, TimeUnit timeUnit)
        {
            keys.add(key);
        }
    }
}
