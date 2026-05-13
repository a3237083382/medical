package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizFeeFlow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long companyId;

    @Excel(name = "公司名称")
    private String companyName;

    @Excel(name = "操作类型", readConverterExp = "RECHARGE=充值,DEDUCT=扣费,SETTLEMENT=扣费结算,REFUND=退款,ADJUST=冲正")
    private String operationType;

    @Excel(name = "金额")
    private BigDecimal amount;

    @Excel(name = "操作前余额")
    private BigDecimal balanceBefore;

    @Excel(name = "操作后余额")
    private BigDecimal balanceAfter;

    @Excel(name = "操作人")
    private String operator;

    private Long bizId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public Date getOperationTime() { return operationTime; }
    public void setOperationTime(Date operationTime) { this.operationTime = operationTime; }
}
