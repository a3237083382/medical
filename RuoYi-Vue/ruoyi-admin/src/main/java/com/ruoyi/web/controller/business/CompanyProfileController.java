package com.ruoyi.web.controller.business;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/api/profile")
public class CompanyProfileController extends BaseController
{
    @Autowired
    private IBizInsuranceCompanyService companyService;

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
        result.put("appKey", company.getAppKey());
        result.put("balance", company.getBalance());
        result.put("billingCycleDays", company.getBillingCycleDays());
        result.put("balanceUpdateTime", company.getBalanceUpdateTime());
        result.put("contactPerson", company.getContactPerson());
        result.put("contactPhone", company.getContactPhone());
        result.put("remark", company.getRemark());
        return result;
    }
}
