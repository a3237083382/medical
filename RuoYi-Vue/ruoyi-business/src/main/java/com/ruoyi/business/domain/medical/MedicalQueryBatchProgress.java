package com.ruoyi.business.domain.medical;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MedicalQueryBatchProgress
{
    private Long batchId;
    private String batchNo;
    private String serviceMode;
    private String batchStatus;
    private int totalCount;
    private int pendingCount;
    private int processingCount;
    private int completedCount;
    private int hitCount;
    private int noResultCount;
    private int failedCount;
    private int cancelledCount;
    private int cancellableCount;
    private BigDecimal totalFee;
    private Date submittedTime;

    @JsonIgnore
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getServiceMode() { return serviceMode; }
    public void setServiceMode(String serviceMode) { this.serviceMode = serviceMode; }
    public String getBatchStatus() { return batchStatus; }
    public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
    public int getProcessingCount() { return processingCount; }
    public void setProcessingCount(int processingCount) { this.processingCount = processingCount; }
    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
    public int getHitCount() { return hitCount; }
    public void setHitCount(int hitCount) { this.hitCount = hitCount; }
    public int getNoResultCount() { return noResultCount; }
    public void setNoResultCount(int noResultCount) { this.noResultCount = noResultCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public int getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }
    public int getCancellableCount() { return cancellableCount; }
    public void setCancellableCount(int cancellableCount) { this.cancellableCount = cancellableCount; }
    public BigDecimal getTotalFee() { return totalFee; }
    public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }
    public Date getSubmittedTime() { return submittedTime; }
    public void setSubmittedTime(Date submittedTime) { this.submittedTime = submittedTime; }
    public boolean isCancellable() { return cancellableCount > 0; }
}
