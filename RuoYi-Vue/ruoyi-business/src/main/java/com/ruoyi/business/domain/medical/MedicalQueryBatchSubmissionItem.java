package com.ruoyi.business.domain.medical;

public class MedicalQueryBatchSubmissionItem
{
    private Integer rowNo;
    private String requestNo;
    private boolean reused;
    private String processStatus;
    private String uploadStatus;

    public Integer getRowNo() { return rowNo; }
    public void setRowNo(Integer rowNo) { this.rowNo = rowNo; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public boolean isReused() { return reused; }
    public void setReused(boolean reused) { this.reused = reused; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }
}
