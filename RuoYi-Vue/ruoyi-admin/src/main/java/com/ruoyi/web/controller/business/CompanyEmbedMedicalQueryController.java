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
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.business.service.IBizQueryPriceService;
import com.ruoyi.business.service.IMedicalQueryService;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import com.ruoyi.business.domain.BizMonthlyUsage;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;

@Anonymous
@RestController
@RequestMapping("/company/embed/medical")
public class CompanyEmbedMedicalQueryController
{
    @Autowired
    private IBizInsuranceCompanyService companyService;

    @Autowired
    private IBizQueryPriceService priceService;

    @Autowired
    private IMedicalQueryService medicalQueryService;

    @GetMapping("/query-types")
    public AjaxResult queryTypes(HttpServletRequest request)
    {
        BizInsuranceCompany company = resolveCompany(request);
        if (company == null)
        {
            return AjaxResult.error(401, "invalid appKey");
        }

        BizQueryPrice filter = new BizQueryPrice();
        filter.setStatus("0");
        return AjaxResult.success(priceService.selectBizQueryPriceList(filter));
    }

    @PostMapping("/query")
    public AjaxResult query(@RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        BizInsuranceCompany company = resolveCompany(request);
        if (company == null)
        {
            return AjaxResult.error(401, "invalid appKey");
        }

        String queryType = toString(body.get("queryType"));
        Map<String, Object> queryParams = toMap(body.get("queryParams"));
        if (StringUtils.isEmpty(queryType) || StringUtils.isEmpty(toString(queryParams.get("name")))
                || StringUtils.isEmpty(toString(queryParams.get("idCard"))))
        {
            return AjaxResult.error(400, "queryType, name and idCard are required");
        }

        MedicalQueryRequest queryRequest = new MedicalQueryRequest();
        queryRequest.setCompanyId(company.getId());
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

    private BizInsuranceCompany resolveCompany(HttpServletRequest request)
    {
        String appKey = request.getHeader("X-App-Key");
        if (StringUtils.isEmpty(appKey))
        {
            return null;
        }
        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey.trim());
        if (company == null || !"0".equals(company.getStatus()))
        {
            return null;
        }
        return company;
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

    @Autowired
    private BizMonthlyUsageMapper monthlyUsageMapper;

    @GetMapping("/usage")
    public AjaxResult usage(HttpServletRequest request)
    {
        BizInsuranceCompany company = resolveCompany(request);
        if (company == null)
        {
            return AjaxResult.error(401, "invalid appKey");
        }

        String billingMonth = YearMonth.now().toString();
        BigDecimal budget = company.getMonthlyBudget() == null ? BigDecimal.ZERO : company.getMonthlyBudget();

        BizMonthlyUsage usage = monthlyUsageMapper.selectUsage(company.getId(), billingMonth);
        BigDecimal usedAmount = BigDecimal.ZERO;
        BigDecimal reservedAmount = BigDecimal.ZERO;
        if (usage != null)
        {
            usedAmount = usage.getUsedAmount() == null ? BigDecimal.ZERO : usage.getUsedAmount();
            reservedAmount = usage.getReservedAmount() == null ? BigDecimal.ZERO : usage.getReservedAmount();
        }

        BigDecimal remaining = budget.subtract(usedAmount).subtract(reservedAmount);
        if (remaining.compareTo(BigDecimal.ZERO) < 0)
        {
            remaining = BigDecimal.ZERO;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("budget", budget);
        data.put("usedAmount", usedAmount);
        data.put("reservedAmount", reservedAmount);
        data.put("remaining", remaining);
        data.put("usagePercent", calcPercent(usedAmount.add(reservedAmount), budget));
        data.put("billingMonth", billingMonth);
        data.put("serviceStatus", remaining.compareTo(BigDecimal.ZERO) <= 0 ? "LIMIT_REACHED" : "NORMAL");
        return AjaxResult.success(data);
    }

    private int calcPercent(BigDecimal used, BigDecimal total)
    {
        if (total == null || BigDecimal.ZERO.compareTo(total) == 0)
        {
            return 0;
        }
        return Math.min(100, used.multiply(new BigDecimal("100")).divide(total, 0, java.math.RoundingMode.DOWN).intValue());
    }
}
