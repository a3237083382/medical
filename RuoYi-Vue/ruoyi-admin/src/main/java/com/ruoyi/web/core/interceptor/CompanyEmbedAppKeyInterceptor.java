package com.ruoyi.web.core.interceptor;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.core.CompanyEmbedRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 使用 X-App-Key 认证保险公司嵌入接口。
 */
@Component
public class CompanyEmbedAppKeyInterceptor implements HandlerInterceptor
{
    public static final String APP_KEY_HEADER = "X-App-Key";

    private final IBizInsuranceCompanyService companyService;

    public CompanyEmbedAppKeyInterceptor(IBizInsuranceCompanyService companyService)
    {
        this.companyService = companyService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException
    {
        if (HttpMethod.OPTIONS.matches(request.getMethod()))
        {
            return true;
        }

        String appKey = StringUtils.trim(request.getHeader(APP_KEY_HEADER));
        if (StringUtils.isEmpty(appKey))
        {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_APP_KEY", "AppKey缺失或无效");
            return false;
        }

        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey);
        if (company == null)
        {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_APP_KEY", "AppKey缺失或无效");
            return false;
        }
        if (!"0".equals(company.getStatus()))
        {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "COMPANY_DISABLED", "保险公司已停用");
            return false;
        }

        request.setAttribute(CompanyEmbedRequestContext.COMPANY_ATTRIBUTE, company);
        return true;
    }

    private void writeError(HttpServletResponse response, int status, String errorCode, String message) throws IOException
    {
        AjaxResult result = AjaxResult.error(status, message).put("errorCode", errorCode);
        response.setStatus(status);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
