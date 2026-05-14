package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ruoyi.business.domain.dashboard.BusinessDashboardSummary;
import com.ruoyi.business.domain.dashboard.BusinessStatItem;
import com.ruoyi.business.domain.dashboard.BusinessTrendItem;
import com.ruoyi.business.mapper.BusinessDashboardMapper;
import com.ruoyi.business.service.IBusinessDashboardService;

@Service
public class BusinessDashboardServiceImpl implements IBusinessDashboardService
{
    private final BusinessDashboardMapper dashboardMapper;

    public BusinessDashboardServiceImpl(BusinessDashboardMapper dashboardMapper)
    {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public BusinessDashboardSummary summary()
    {
        BusinessDashboardSummary summary = new BusinessDashboardSummary();
        summary.setTotalQueryCount(nvl(dashboardMapper.selectTotalQueryCount()));
        summary.setSuccessQueryCount(nvl(dashboardMapper.selectQueryCountByStatus("0")));
        summary.setFailedQueryCount(nvl(dashboardMapper.selectQueryCountByStatus("1")));
        summary.setCompanyCount(nvl(dashboardMapper.selectCompanyCount()));
        summary.setActiveCompanyCount(nvl(dashboardMapper.selectCompanyCountByStatus("0")));
        summary.setTodayQueryCount(nvl(dashboardMapper.selectTodayQueryCount()));
        summary.setTotalFee(nvl(dashboardMapper.selectTotalFee()));
        return summary;
    }

    @Override
    public List<BusinessTrendItem> monthlyTrend()
    {
        return dashboardMapper.selectMonthlyTrend();
    }

    @Override
    public List<BusinessStatItem> queryTypeStats()
    {
        return dashboardMapper.selectQueryTypeStats();
    }

    @Override
    public List<BusinessStatItem> companyRank()
    {
        return dashboardMapper.selectCompanyRank();
    }

    private Long nvl(Long value)
    {
        return value == null ? 0L : value;
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}

