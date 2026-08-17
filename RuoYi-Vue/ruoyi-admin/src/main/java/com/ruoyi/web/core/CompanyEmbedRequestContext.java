package com.ruoyi.web.core;

import com.ruoyi.business.domain.BizInsuranceCompany;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 当前保险公司嵌入接口的请求上下文。
 */
public final class CompanyEmbedRequestContext
{
    public static final String COMPANY_ATTRIBUTE = CompanyEmbedRequestContext.class.getName() + ".company";

    private CompanyEmbedRequestContext()
    {
    }

    public static BizInsuranceCompany getCompany(HttpServletRequest request)
    {
        Object company = request.getAttribute(COMPANY_ATTRIBUTE);
        return company instanceof BizInsuranceCompany ? (BizInsuranceCompany) company : null;
    }
}
