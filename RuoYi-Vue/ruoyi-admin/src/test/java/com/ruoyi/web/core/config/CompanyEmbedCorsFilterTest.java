package com.ruoyi.web.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.CorsFilter;
import com.ruoyi.framework.config.ResourcesConfig;

public class CompanyEmbedCorsFilterTest
{
    private CorsFilter corsFilter;

    @BeforeEach
    public void setUp()
    {
        ResourcesConfig resourcesConfig = new ResourcesConfig();
        ReflectionTestUtils.setField(resourcesConfig, "embeddedAllowedOriginPatterns",
                "http://localhost:[*],https://insurance.example.com");
        corsFilter = resourcesConfig.corsFilter();
    }

    @Test
    public void allowsConfiguredOriginAndAppKeyHeader() throws Exception
    {
        MockHttpServletRequest request = preflight("/company/embed/medical/query", "http://localhost:5173");
        MockHttpServletResponse response = new MockHttpServletResponse();

        corsFilter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        assertEquals("http://localhost:5173", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("X-App-Key", response.getHeader("Access-Control-Allow-Headers"));
    }

    @Test
    public void rejectsUnconfiguredOriginForCompanyEmbedApi() throws Exception
    {
        MockHttpServletRequest request = preflight("/company/embed/medical/query", "https://untrusted.example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        corsFilter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertNull(response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void preservesExistingCorsBehaviorOutsideCompanyEmbedApi() throws Exception
    {
        MockHttpServletRequest request = preflight("/company/api/profile", "https://portal.example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();

        corsFilter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        assertEquals("https://portal.example.com", response.getHeader("Access-Control-Allow-Origin"));
    }

    private MockHttpServletRequest preflight(String uri, String origin)
    {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", uri);
        request.addHeader("Origin", origin);
        request.addHeader("Access-Control-Request-Method", "GET");
        request.addHeader("Access-Control-Request-Headers", "X-App-Key");
        return request;
    }
}
