package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;

import com.ruoyi.business.domain.dashboard.BusinessStatItem;
import com.ruoyi.business.domain.dashboard.BusinessTrendItem;

public interface BusinessDashboardMapper
{
    Long selectTotalQueryCount();

    Long selectQueryCountByStatus(String status);

    BigDecimal selectTotalFee();

    Long selectCompanyCount();

    Long selectCompanyCountByStatus(String status);

    Long selectTodayQueryCount();

    List<BusinessTrendItem> selectMonthlyTrend();

    List<BusinessStatItem> selectQueryTypeStats();

    List<BusinessStatItem> selectCompanyRank();
}

