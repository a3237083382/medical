package com.ruoyi.business.domain.medical;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class MedicalQueryRequestDetail
{
    private String requestNo;
    private String entryType;
    private String serviceMode;
    private String queryType;
    private String patientName;
    private String idCard;
    private String processStatus;
    private String uploadStatus;
    private String resultStatus;
    private String viewStatus;
    private BigDecimal fee;
    private Date submittedTime;
    private Date processStartTime;
    private Date completeTime;
    private boolean resultVisible;
    private Object columnSchema;
    private Map<String, Object> data;
    private String resultSummary;

    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public String getServiceMode() { return serviceMode; }
    public void setServiceMode(String serviceMode) { this.serviceMode = serviceMode; }
    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }
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
    public String getViewStatus() { return viewStatus; }
    public void setViewStatus(String viewStatus) { this.viewStatus = viewStatus; }
    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }
    public Date getSubmittedTime() { return submittedTime; }
    public void setSubmittedTime(Date submittedTime) { this.submittedTime = submittedTime; }
    public Date getProcessStartTime() { return processStartTime; }
    public void setProcessStartTime(Date processStartTime) { this.processStartTime = processStartTime; }
    public Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(Date completeTime) { this.completeTime = completeTime; }
    public boolean isResultVisible() { return resultVisible; }
    public void setResultVisible(boolean resultVisible) { this.resultVisible = resultVisible; }
    public Object getColumnSchema() { return columnSchema; }
    public void setColumnSchema(Object columnSchema) { this.columnSchema = columnSchema; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
}
