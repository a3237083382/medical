package com.ruoyi.business.domain.medical;

import java.util.Date;
import java.util.Map;
import com.ruoyi.business.domain.BizMedicalQueryRequest;

public class DelayedMedicalQueryAdminDetail
{
    private BizMedicalQueryRequest request;
    private boolean hasResult;
    private Object columnSchema;
    private Map<String, Object> data;
    private String resultSummary;
    private Integer resultVersion;
    private String uploadedBy;
    private Date uploadedTime;
    private String updateBy;
    private Date updateTime;
    private String updateReason;

    public BizMedicalQueryRequest getRequest() { return request; }
    public void setRequest(BizMedicalQueryRequest request) { this.request = request; }
    public boolean isHasResult() { return hasResult; }
    public void setHasResult(boolean hasResult) { this.hasResult = hasResult; }
    public Object getColumnSchema() { return columnSchema; }
    public void setColumnSchema(Object columnSchema) { this.columnSchema = columnSchema; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
    public Integer getResultVersion() { return resultVersion; }
    public void setResultVersion(Integer resultVersion) { this.resultVersion = resultVersion; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public Date getUploadedTime() { return uploadedTime; }
    public void setUploadedTime(Date uploadedTime) { this.uploadedTime = uploadedTime; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
}
