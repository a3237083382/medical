package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;

public class BizMedicalQueryBatchItem
{
    private Long id;
    private Long batchId;
    private Long requestId;
    private String requestNo;
    private Integer rowNo;
    private String reusedFlag;
    private String itemStatus;
    private String patientName;
    private String idCard;
    private String processStatus;
    private String uploadStatus;
    private String resultStatus;
    private BigDecimal feeSnapshot;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public Integer getRowNo() { return rowNo; }
    public void setRowNo(Integer rowNo) { this.rowNo = rowNo; }
    public String getReusedFlag() { return reusedFlag; }
    public void setReusedFlag(String reusedFlag) { this.reusedFlag = reusedFlag; }
    public String getItemStatus() { return itemStatus; }
    public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }
    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    public BigDecimal getFeeSnapshot() { return feeSnapshot; }
    public void setFeeSnapshot(BigDecimal feeSnapshot) { this.feeSnapshot = feeSnapshot; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
