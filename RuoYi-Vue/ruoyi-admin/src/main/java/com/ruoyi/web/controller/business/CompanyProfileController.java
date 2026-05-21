package com.ruoyi.web.controller.business;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMonthlyUsage;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.BillingCycleConfigService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/api/profile")
public class CompanyProfileController extends BaseController
{
    @Autowired
    private IBizInsuranceCompanyService companyService;

    @Autowired
    private BillingCycleConfigService billingCycleConfigService;

    @Autowired
    private BizMonthlyUsageMapper monthlyUsageMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping
    public AjaxResult getProfile(HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        return AjaxResult.success(toProfile(companyService.selectBizInsuranceCompanyById(companyId)));
    }

    @PutMapping
    public AjaxResult updateProfile(@RequestBody BizInsuranceCompany form, HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(companyId);
        company.setContactPerson(form.getContactPerson());
        company.setContactPhone(form.getContactPhone());
        company.setRemark(form.getRemark());
        return toAjax(companyService.updateBizInsuranceCompany(company));
    }

    @PutMapping("/password")
    public AjaxResult updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty())
        {
            return AjaxResult.error("密码不能为空");
        }
        if (newPassword.length() < 6)
        {
            return AjaxResult.error("新密码至少6位");
        }

        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyById(companyId);
        if (company == null || company.getPassword() == null
                || !passwordEncoder.matches(oldPassword, company.getPassword()))
        {
            return AjaxResult.error("原密码错误");
        }
        return toAjax(companyService.updatePassword(companyId, newPassword));
    }

    @PostMapping("/app-key")
    public AjaxResult regenerateAppKey(HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        String appKey = companyService.regenerateAppKey(companyId);
        Map<String, Object> result = new HashMap<>();
        result.put("appKey", appKey);
        result.put("appKeyMasked", maskAppKey(appKey));
        return AjaxResult.success(result);
    }

    private Map<String, Object> toProfile(BizInsuranceCompany company)
    {
        Map<String, Object> result = new HashMap<>();
        if (company == null)
        {
            return result;
        }
        result.put("id", company.getId());
        result.put("companyName", company.getCompanyName());
        result.put("companyCode", company.getCompanyCode());
        result.put("username", company.getUsername());
        result.put("hasAppKey", company.getAppKey() != null && !company.getAppKey().isEmpty());
        result.put("appKeyMasked", maskAppKey(company.getAppKey()));
        result.put("balance", company.getBalance());
        result.put("billingCycleDays", billingCycleConfigService.getBillingCycleDays());
        result.put("balanceUpdateTime", company.getBalanceUpdateTime());
        result.put("monthlyBudget", company.getMonthlyBudget());
        result.put("budgetEnabled", company.getBudgetEnabled());
        putUsageStatus(result, company);
        result.put("contactPerson", company.getContactPerson());
        result.put("contactPhone", company.getContactPhone());
        result.put("remark", company.getRemark());
        return result;
    }

    private void putUsageStatus(Map<String, Object> result, BizInsuranceCompany company)
    {
        String billingMonth = YearMonth.now().toString();
        BigDecimal monthlyBudget = company.getMonthlyBudget() == null ? BigDecimal.ZERO : company.getMonthlyBudget();
        BigDecimal usedAmount = BigDecimal.ZERO;
        BigDecimal reservedAmount = BigDecimal.ZERO;
        BizMonthlyUsage usage = monthlyUsageMapper.selectUsage(company.getId(), billingMonth);
        if (usage != null)
        {
            usedAmount = usage.getUsedAmount() == null ? BigDecimal.ZERO : usage.getUsedAmount();
            reservedAmount = usage.getReservedAmount() == null ? BigDecimal.ZERO : usage.getReservedAmount();
        }
        BigDecimal activeAmount = usedAmount.add(reservedAmount);
        int usagePercent = BigDecimal.ZERO.compareTo(monthlyBudget) == 0 ? 0
                : activeAmount.multiply(new BigDecimal("100")).divide(monthlyBudget, 0, java.math.RoundingMode.DOWN).intValue();
        result.put("billingMonth", billingMonth);
        result.put("usedAmount", usedAmount);
        result.put("reservedAmount", reservedAmount);
        result.put("usagePercent", Math.min(usagePercent, 100));
        result.put("serviceStatus", usagePercent >= 100 ? "LIMIT_REACHED" : usagePercent >= 80 ? "NEAR_LIMIT" : "NORMAL");
    }

    private String maskAppKey(String value)
    {
        if (value == null || value.isEmpty())
        {
            return "-";
        }
        if (value.length() <= 8)
        {
            return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}
