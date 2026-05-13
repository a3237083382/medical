package com.ruoyi.business.config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.business.util.SignUtil;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.http.HttpHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SignAuthInterceptor implements HandlerInterceptor
{
    private static final long ALLOWED_CLOCK_SKEW_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private static final int NONCE_TTL_MINUTES = 6;
    private static final String NONCE_PREFIX = "api_sign_nonce:";

    private final IBizInsuranceCompanyService companyService;
    private final RedisCache redisCache;

    public SignAuthInterceptor(IBizInsuranceCompanyService companyService, RedisCache redisCache)
    {
        this.companyService = companyService;
        this.redisCache = redisCache;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String appKey = request.getHeader("X-App-Key");
        String timestamp = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        String sign = request.getHeader("X-Sign");

        if (isBlank(appKey) || isBlank(timestamp) || isBlank(nonce) || isBlank(sign))
        {
            return reject(response, "missing sign headers");
        }

        Long timestampMillis = parseTimestamp(timestamp);
        if (timestampMillis == null || Math.abs(System.currentTimeMillis() - timestampMillis) > ALLOWED_CLOCK_SKEW_MILLIS)
        {
            return reject(response, "timestamp expired");
        }

        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey);
        if (company == null || "1".equals(company.getStatus()) || isBlank(company.getAppSecret()))
        {
            return reject(response, "invalid app key");
        }

        String nonceKey = NONCE_PREFIX + appKey + ":" + nonce;
        if (Boolean.TRUE.equals(redisCache.hasKey(nonceKey)))
        {
            return reject(response, "nonce reused");
        }

        String body = HttpHelper.getBodyString(request);
        if (!SignUtil.verify(timestamp, nonce, body, company.getAppSecret(), sign))
        {
            return reject(response, "invalid sign");
        }

        redisCache.setCacheObject(nonceKey, "1", NONCE_TTL_MINUTES, TimeUnit.MINUTES);
        request.setAttribute("companyId", company.getId());
        request.setAttribute("companyName", company.getCompanyName());
        request.setAttribute("company", company);
        return true;
    }

    private boolean reject(HttpServletResponse response, String message) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + message + "\"}");
        return false;
    }

    private Long parseTimestamp(String timestamp)
    {
        try
        {
            return Long.parseLong(timestamp);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
