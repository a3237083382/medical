package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizMedicalQueryBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String batchNo;
    private Long companyId;
    private String companyName;
    private String serviceMode;
    private String queryType;
    private String batchStatus;
    private Integer totalCount;
    private Integer pendingCount;
    private Integer processingCount;
    private Integer completedCount;
    private Integer hitCount;
    private Integer noResultCount;
    private Integer failedCount;
    private Integer cancelledCount;
    private BigDecimal totalFee;
    private String requestIp;
    private Date createTime;
    private Date completeTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getServiceMode() { return serviceMode; }
    public void setServiceMode(String serviceMode) { this.serviceMode = serviceMode; }
    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }
    public String getBatchStatus() { return batchStatus; }
    public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getPendingCount() { return pendingCount; }
    public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
    public Integer getProcessingCount() { return processingCount; }
    public void setProcessingCount(Integer processingCount) { this.processingCount = processingCount; }
    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }
    public Integer getHitCount() { return hitCount; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }
    public Integer getNoResultCount() { return noResultCount; }
    public void setNoResultCount(Integer noResultCount) { this.noResultCount = noResultCount; }
    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }
    public Integer getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(Integer cancelledCount) { this.cancelledCount = cancelledCount; }
    public BigDecimal getTotalFee() { return totalFee; }
    public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(Date completeTime) { this.completeTime = completeTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
