package com.ruoyi.web.controller.business;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.config.CompanyTokenService;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.web.service.BillingCycleConfigService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company")
public class CompanyAuthController extends BaseController
{
    @Autowired
    private IBizInsuranceCompanyService companyService;

    @Autowired
    private CompanyTokenService tokenService;

    @Autowired
    private BillingCycleConfigService billingCycleConfigService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> params, HttpServletRequest request)
    {
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || password == null)
        {
            return AjaxResult.error("用户名或密码不能为空");
        }

        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByUsername(username);
        if (company == null)
        {
            return AjaxResult.error("用户名或密码错误");
        }

        if ("1".equals(company.getStatus()))
        {
            return AjaxResult.error("该账户已被停用，请联系管理员");
        }

        // 验证密码
        if (company.getPassword() == null || !passwordEncoder.matches(password, company.getPassword()))
        {
            return AjaxResult.error("用户名或密码错误");
        }

        // 更新登录信息
        company.setLoginIp(IpUtils.getIpAddr());
        company.setLoginDate(new Date());
        companyService.updateLoginInfo(company);

        // 生成token
        String token = tokenService.createToken(company);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("companyId", company.getId());
        result.put("companyName", company.getCompanyName());
        result.put("balance", company.getBalance());
        result.put("monthlyBudget", company.getMonthlyBudget());
        result.put("budgetEnabled", company.getBudgetEnabled());
        result.put("balanceUpdateTime", company.getBalanceUpdateTime());
        result.put("billingCycleDays", billingCycleConfigService.getBillingCycleDays());

        return AjaxResult.success(result);
    }
}
