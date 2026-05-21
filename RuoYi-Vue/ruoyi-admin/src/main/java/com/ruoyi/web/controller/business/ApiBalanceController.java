package com.ruoyi.web.controller.business;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMonthlyUsage;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/api/v1/balance")
public class ApiBalanceController
{
    private final IBizInsuranceCompanyService companyService;
    private final BizMonthlyUsageMapper monthlyUsageMapper;

    public ApiBalanceController(IBizInsuranceCompanyService companyService, BizMonthlyUsageMapper monthlyUsageMapper)
    {
        this.companyService = companyService;
        this.monthlyUsageMapper = monthlyUsageMapper;
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
        data.put("billingMonth", YearMonth.now().toString());
        data.put("serviceStatus", serviceStatus(company));
        data.put("usagePercent", usagePercent(company));
        return AjaxResult.success(data);
    }

    private String serviceStatus(BizInsuranceCompany company)
    {
        int percent = usagePercent(company);
        if (percent >= 100)
        {
            return "LIMIT_REACHED";
        }
        if (percent >= 80)
        {
            return "NEAR_LIMIT";
        }
        return "NORMAL";
    }

    private int usagePercent(BizInsuranceCompany company)
    {
        BigDecimal budget = company.getMonthlyBudget() == null ? BigDecimal.ZERO : company.getMonthlyBudget();
        if (BigDecimal.ZERO.compareTo(budget) == 0)
        {
            return 0;
        }
        BizMonthlyUsage usage = monthlyUsageMapper.selectUsage(company.getId(), YearMonth.now().toString());
        BigDecimal used = BigDecimal.ZERO;
        if (usage != null)
        {
            used = used.add(usage.getUsedAmount() == null ? BigDecimal.ZERO : usage.getUsedAmount());
            used = used.add(usage.getReservedAmount() == null ? BigDecimal.ZERO : usage.getReservedAmount());
        }
        return Math.min(100, used.multiply(new BigDecimal("100")).divide(budget, 0, java.math.RoundingMode.DOWN).intValue());
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
