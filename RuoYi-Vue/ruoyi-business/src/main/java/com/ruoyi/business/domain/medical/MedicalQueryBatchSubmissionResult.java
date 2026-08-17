package com.ruoyi.business.domain.medical;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MedicalQueryBatchSubmissionResult
{
    private String batchNo;
    private String batchStatus;
    private int totalCount;
    private int pendingCount;
    private int processingCount;
    private int reusedCount;
    private BigDecimal totalFee;
    private List<MedicalQueryBatchSubmissionItem> items = new ArrayList<>();

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getBatchStatus() { return batchStatus; }
    public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
    public int getProcessingCount() { return processingCount; }
    public void setProcessingCount(int processingCount) { this.processingCount = processingCount; }
    public int getReusedCount() { return reusedCount; }
    public void setReusedCount(int reusedCount) { this.reusedCount = reusedCount; }
    public BigDecimal getTotalFee() { return totalFee; }
    public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }
    public List<MedicalQueryBatchSubmissionItem> getItems() { return items; }
    public void setItems(List<MedicalQueryBatchSubmissionItem> items) { this.items = items; }
}
