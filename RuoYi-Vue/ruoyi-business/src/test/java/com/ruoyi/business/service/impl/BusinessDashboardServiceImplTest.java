package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ruoyi.business.domain.dashboard.BusinessDashboardSummary;
import com.ruoyi.business.domain.dashboard.BusinessStatItem;
import com.ruoyi.business.domain.dashboard.BusinessTrendItem;
import com.ruoyi.business.mapper.BusinessDashboardMapper;

class BusinessDashboardServiceImplTest
{
    @Test
    void summaryUsesZeroWhenMapperReturnsNull()
    {
        BusinessDashboardServiceImpl service = new BusinessDashboardServiceImpl(new FakeDashboardMapper());

        BusinessDashboardSummary summary = service.summary();

        assertEquals(0L, summary.getTotalQueryCount());
        assertEquals(0L, summary.getSuccessQueryCount());
        assertEquals(0L, summary.getFailedQueryCount());
        assertEquals(0L, summary.getCompanyCount());
        assertEquals(0L, summary.getActiveCompanyCount());
        assertEquals(0L, summary.getTodayQueryCount());
        assertEquals(BigDecimal.ZERO, summary.getTotalFee());
    }

    @Test
    void chartMethodsReturnMapperRows()
    {
        FakeDashboardMapper mapper = new FakeDashboardMapper();
        mapper.monthlyTrend = List.of(trend("2026-05", 12L, "30.00"));
        mapper.queryTypeStats = List.of(item("medical_all", 3L, "6.00"));
        mapper.companyRank = List.of(item("测试保险公司", 8L, "16.00"));
        BusinessDashboardServiceImpl service = new BusinessDashboardServiceImpl(mapper);

        assertEquals("2026-05", service.monthlyTrend().get(0).getName());
        assertEquals("medical_all", service.queryTypeStats().get(0).getName());
        assertEquals("测试保险公司", service.companyRank().get(0).getName());
    }

    private static BusinessTrendItem trend(String name, Long value, String amount)
    {
        BusinessTrendItem item = new BusinessTrendItem();
        item.setName(name);
        item.setValue(value);
        item.setAmount(new BigDecimal(amount));
        return item;
    }

    private static BusinessStatItem item(String name, Long value, String amount)
    {
        BusinessStatItem item = new BusinessStatItem();
        item.setName(name);
        item.setValue(value);
        item.setAmount(new BigDecimal(amount));
        return item;
    }

    private static class FakeDashboardMapper implements BusinessDashboardMapper
    {
        private List<BusinessTrendItem> monthlyTrend = List.of();
        private List<BusinessStatItem> queryTypeStats = List.of();
        private List<BusinessStatItem> companyRank = List.of();

        @Override public Long selectTotalQueryCount() { return null; }
        @Override public Long selectQueryCountByStatus(String status) { return null; }
        @Override public BigDecimal selectTotalFee() { return null; }
        @Override public Long selectCompanyCount() { return null; }
        @Override public Long selectCompanyCountByStatus(String status) { return null; }
        @Override public Long selectTodayQueryCount() { return null; }
        @Override public List<BusinessTrendItem> selectMonthlyTrend() { return monthlyTrend; }
        @Override public List<BusinessStatItem> selectQueryTypeStats() { return queryTypeStats; }
        @Override public List<BusinessStatItem> selectCompanyRank() { return companyRank; }
    }
}
