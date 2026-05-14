package com.ruoyi.web.controller.business;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/api/v1/balance")
public class ApiBalanceController
{
    private final IBizInsuranceCompanyService companyService;

    public ApiBalanceController(IBizInsuranceCompanyService companyService)
    {
        this.companyService = companyService;
    }

    @GetMapping("/query")
    public AjaxResult query(HttpServletRequest request)
    {
        Long companyId = resolveCompanyId(request);
        if (companyId == null)
        {
            return AjaxResult.error(401, "missing company context");
        }

        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyById(companyId);
        if (company == null)
        {
            return AjaxResult.error(4002, "company not found");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("companyId", company.getId());
        data.put("companyName", company.getCompanyName());
        data.put("balance", company.getBalance());
        data.put("balanceUpdateTime", company.getBalanceUpdateTime());
        return AjaxResult.success(data);
    }

    private Long resolveCompanyId(HttpServletRequest request)
    {
        Object companyId = request.getAttribute("companyId");
        if (companyId instanceof Long value)
        {
            return value;
        }
        Object company = request.getAttribute("company");
        if (company instanceof BizInsuranceCompany value)
        {
            return value.getId();
        }
        return null;
    }
}
