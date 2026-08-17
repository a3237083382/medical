package com.ruoyi.business.domain.medical;

import java.math.BigDecimal;

public class MedicalQueryBatchItemState
{
    private Long itemId;
    private Long batchId;
    private String batchNo;
    private Long requestId;
    private String requestNo;
    private String entryType;
    private String reusedFlag;
    private String itemStatus;
    private String processStatus;
    private String uploadStatus;
    private BigDecimal reservedFee;
    private String billingMonth;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public String getReusedFlag() { return reusedFlag; }
    public void setReusedFlag(String reusedFlag) { this.reusedFlag = reusedFlag; }
    public String getItemStatus() { return itemStatus; }
    public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }
    public BigDecimal getReservedFee() { return reservedFee; }
    public void setReservedFee(BigDecimal reservedFee) { this.reservedFee = reservedFee; }
    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }
}
