package com.ruoyi.web.controller.business;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.domain.medical.MedicalQueryResult;
import com.ruoyi.business.service.IBizQueryPriceService;
import com.ruoyi.business.service.IMedicalQueryService;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;

@RestController
@RequestMapping("/company/api/medical")
public class CompanyMedicalQueryController extends BaseController
{
    @Autowired
    private IBizQueryPriceService priceService;

    @Autowired
    private IMedicalQueryService medicalQueryService;

    @GetMapping("/query-types")
    public AjaxResult queryTypes()
    {
        BizQueryPrice filter = new BizQueryPrice();
        filter.setStatus("0");
        return AjaxResult.success(priceService.selectBizQueryPriceList(filter));
    }

    @PostMapping("/query")
    public AjaxResult query(@RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        Long companyId = resolveCompanyId(request);
        if (companyId == null)
        {
            return AjaxResult.error(401, "missing company context");
        }

        String queryType = toString(body.get("queryType"));
        Map<String, Object> queryParams = toMap(body.get("queryParams"));
        if (StringUtils.isEmpty(queryType) || StringUtils.isEmpty(toString(queryParams.get("name")))
                || StringUtils.isEmpty(toString(queryParams.get("idCard"))))
        {
            return AjaxResult.error(400, "queryType, name and idCard are required");
        }

        MedicalQueryRequest queryRequest = new MedicalQueryRequest();
        queryRequest.setCompanyId(companyId);
        queryRequest.setQueryType(queryType);
        queryRequest.setQueryParams(queryParams);
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
        Object companyId = request.getAttribute("companyId");
        if (companyId instanceof Long)
        {
            return (Long) companyId;
        }
        Object company = request.getAttribute("company");
        if (company instanceof BizInsuranceCompany)
        {
            return ((BizInsuranceCompany) company).getId();
        }
        return null;
    }

    private String toString(Object value)
    {
        return value == null ? null : String.valueOf(value).trim();
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
