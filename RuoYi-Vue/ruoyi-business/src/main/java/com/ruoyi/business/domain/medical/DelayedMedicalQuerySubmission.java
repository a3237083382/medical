package com.ruoyi.business.domain.medical;

import java.util.Date;

public class DelayedMedicalQuerySubmission
{
    private String requestNo;
    private String processStatus;
    private String uploadStatus;
    private String resultStatus;
    private boolean reused;
    private Date submittedTime;

    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }
    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    public boolean isReused() { return reused; }
    public void setReused(boolean reused) { this.reused = reused; }
    public Date getSubmittedTime() { return submittedTime; }
    public void setSubmittedTime(Date submittedTime) { this.submittedTime = submittedTime; }
}
