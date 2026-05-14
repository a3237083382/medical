package com.ruoyi.web.controller.business;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.business.service.IBusinessDashboardService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/business/dashboard")
public class BusinessDashboardController extends BaseController
{
    private final IBusinessDashboardService dashboardService;

    public BusinessDashboardController(IBusinessDashboardService dashboardService)
    {
        this.dashboardService = dashboardService;
    }

    @PreAuthorize("@ss.hasPermi('business:dashboard:view')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(dashboardService.summary());
    }

    @PreAuthorize("@ss.hasPermi('business:dashboard:view')")
    @GetMapping("/trend")
    public AjaxResult trend()
    {
        return success(dashboardService.monthlyTrend());
    }

    @PreAuthorize("@ss.hasPermi('business:dashboard:view')")
    @GetMapping("/query-type")
    public AjaxResult queryType()
    {
        return success(dashboardService.queryTypeStats());
    }

    @PreAuthorize("@ss.hasPermi('business:dashboard:view')")
    @GetMapping("/company-rank")
    public AjaxResult companyRank()
    {
        return success(dashboardService.companyRank());
    }
}

