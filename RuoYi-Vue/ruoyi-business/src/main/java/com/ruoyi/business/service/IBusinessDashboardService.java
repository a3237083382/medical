package com.ruoyi.business.service;

import java.util.List;

import com.ruoyi.business.domain.dashboard.BusinessDashboardSummary;
import com.ruoyi.business.domain.dashboard.BusinessStatItem;
import com.ruoyi.business.domain.dashboard.BusinessTrendItem;

public interface IBusinessDashboardService
{
    BusinessDashboardSummary summary();

    List<BusinessTrendItem> monthlyTrend();

    List<BusinessStatItem> queryTypeStats();

    List<BusinessStatItem> companyRank();
}

