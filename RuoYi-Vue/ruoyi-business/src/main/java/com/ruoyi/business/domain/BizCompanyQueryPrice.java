package com.ruoyi.business.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizCompanyQueryPrice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long companyId;
    private String companyName;
    private String queryType;
    private String queryName;

    @Excel(name = "Hit fee", cellType = ColumnType.NUMERIC)
    private BigDecimal hitFee;

    @Excel(name = "No result fee", cellType = ColumnType.NUMERIC)
    private BigDecimal noResultFee;

    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public String getQueryName() { return queryName; }
    public void setQueryName(String queryName) { this.queryName = queryName; }

    public BigDecimal getHitFee() { return hitFee; }
    public void setHitFee(BigDecimal hitFee) { this.hitFee = hitFee; }

    public BigDecimal getNoResultFee() { return noResultFee; }
    public void setNoResultFee(BigDecimal noResultFee) { this.noResultFee = noResultFee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
