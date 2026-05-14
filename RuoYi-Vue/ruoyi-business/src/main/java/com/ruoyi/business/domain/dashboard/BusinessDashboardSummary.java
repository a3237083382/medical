package com.ruoyi.business.domain.dashboard;

import java.math.BigDecimal;

public class BusinessDashboardSummary
{
    private Long totalQueryCount;
    private Long successQueryCount;
    private Long failedQueryCount;
    private Long companyCount;
    private Long activeCompanyCount;
    private Long todayQueryCount;
    private BigDecimal totalFee;

    public Long getTotalQueryCount() { return totalQueryCount; }
    public void setTotalQueryCount(Long totalQueryCount) { this.totalQueryCount = totalQueryCount; }
    public Long getSuccessQueryCount() { return successQueryCount; }
    public void setSuccessQueryCount(Long successQueryCount) { this.successQueryCount = successQueryCount; }
    public Long getFailedQueryCount() { return failedQueryCount; }
    public void setFailedQueryCount(Long failedQueryCount) { this.failedQueryCount = failedQueryCount; }
    public Long getCompanyCount() { return companyCount; }
    public void setCompanyCount(Long companyCount) { this.companyCount = companyCount; }
    public Long getActiveCompanyCount() { return activeCompanyCount; }
    public void setActiveCompanyCount(Long activeCompanyCount) { this.activeCompanyCount = activeCompanyCount; }
    public Long getTodayQueryCount() { return todayQueryCount; }
    public void setTodayQueryCount(Long todayQueryCount) { this.todayQueryCount = todayQueryCount; }
    public BigDecimal getTotalFee() { return totalFee; }
    public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }
}

