package com.ruoyi.web.controller.business;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.domain.medical.MedicalQueryResult;
import com.ruoyi.business.service.IMedicalQueryService;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/api/v1/medical")
public class ApiMedicalQueryController
{
    private final IMedicalQueryService medicalQueryService;
    private final IBizInsuranceCompanyService companyService;

    public ApiMedicalQueryController(IMedicalQueryService medicalQueryService, IBizInsuranceCompanyService companyService)
    {
        this.medicalQueryService = medicalQueryService;
        this.companyService = companyService;
    }

    @PostMapping("/query")
    public AjaxResult query(@RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        Long companyId = resolveCompanyId(request);
        if (companyId == null)
        {
            return AjaxResult.error(401, "missing company context");
        }

        MedicalQueryRequest queryRequest = new MedicalQueryRequest();
        queryRequest.setCompanyId(companyId);
        queryRequest.setQueryType(toString(body.get("queryType")));
        queryRequest.setQueryParams(toMap(body.get("queryParams")));
        queryRequest.setRequestIp(request.getRemoteAddr());
        try
        {
            MedicalQueryResult result = medicalQueryService.query(queryRequest);
            return AjaxResult.success(result);
        }
        catch (MedicalQueryException e)
        {
            return AjaxResult.error(toCode(e.getCode()), e.getMessage());
        }
    }

    private Long resolveCompanyId(HttpServletRequest request)
    {
        String appKey = request.getHeader("X-App-Key");
        if (com.ruoyi.common.utils.StringUtils.isEmpty(appKey))
        {
            return null;
        }
        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey.trim());
        if (company == null || !"0".equals(company.getStatus()))
        {
            return null;
        }
        return company.getId();
    }

    private String toString(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value)
    {
        if (value instanceof Map<?, ?>)
        {
            return (Map<String, Object>) value;
        }
        return new LinkedHashMap<>();
    }

    private int toCode(String code)
    {
        try
        {
            return Integer.parseInt(code);
        }
        catch (NumberFormatException e)
        {
            return 500;
        }
    }
}
