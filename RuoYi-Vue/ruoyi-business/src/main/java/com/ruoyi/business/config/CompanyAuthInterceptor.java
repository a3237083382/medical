package com.ruoyi.business.config;

import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.config.CompanyTokenService.CompanySession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CompanyAuthInterceptor implements HandlerInterceptor
{
    @Autowired
    private CompanyTokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (!(handler instanceof HandlerMethod))
        {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"msg\":\"未登录或token已过期\",\"code\":401}");
            return false;
        }

        String jwtToken = authHeader.substring(7);
        CompanySession session = tokenService.getSessionFromToken(jwtToken);
        BizInsuranceCompany company = session == null ? null : session.getCompany();
        if (company == null)
        {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"msg\":\"token无效或已过期\",\"code\":401}");
            return false;
        }

        if ("1".equals(company.getStatus()))
        {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"msg\":\"账户已被停用\",\"code\":403}");
            return false;
        }

        request.setAttribute("companyId", company.getId());
        request.setAttribute("companyName", company.getCompanyName());
        request.setAttribute("company", company);
        return true;
    }
}
