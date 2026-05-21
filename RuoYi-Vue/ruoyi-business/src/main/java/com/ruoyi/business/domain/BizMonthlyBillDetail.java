package com.ruoyi.business.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizMonthlyBillDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long billId;
    private Long companyId;
    private String billingMonth;
    private String queryType;
    private String queryName;
    private String resultStatus;
    private Integer queryCount;
    private BigDecimal totalAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public String getQueryName() { return queryName; }
    public void setQueryName(String queryName) { this.queryName = queryName; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public Integer getQueryCount() { return queryCount; }
    public void setQueryCount(Integer queryCount) { this.queryCount = queryCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
