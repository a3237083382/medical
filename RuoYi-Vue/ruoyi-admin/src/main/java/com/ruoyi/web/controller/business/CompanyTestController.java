package com.ruoyi.web.controller.business;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/api")
public class CompanyTestController
{
    @GetMapping("/test")
    public AjaxResult test(HttpServletRequest request)
    {
        String companyName = (String) request.getAttribute("companyName");
        Long companyId = (Long) request.getAttribute("companyId");
        return AjaxResult.success("认证成功: " + companyName + "(ID:" + companyId + ")");
    }
}
