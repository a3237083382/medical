package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizRechargeRequest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long companyId;

    @Excel(name = "公司名称")
    private String companyName;

    @Excel(name = "充值金额", cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal amount;

    /** 状态（0待审核 1已通过 2已驳回） */
    @Excel(name = "状态", readConverterExp = "0=待审核,1=已通过,2=已驳回")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    @Excel(name = "提交备注")
    private String submitRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    @Excel(name = "审核人")
    private String reviewer;

    @Excel(name = "审核备注")
    private String reviewRemark;

    private Long feeFlowId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public String getSubmitRemark() { return submitRemark; }
    public void setSubmitRemark(String submitRemark) { this.submitRemark = submitRemark; }
    public Date getReviewTime() { return reviewTime; }
    public void setReviewTime(Date reviewTime) { this.reviewTime = reviewTime; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public Long getFeeFlowId() { return feeFlowId; }
    public void setFeeFlowId(Long feeFlowId) { this.feeFlowId = feeFlowId; }
}
