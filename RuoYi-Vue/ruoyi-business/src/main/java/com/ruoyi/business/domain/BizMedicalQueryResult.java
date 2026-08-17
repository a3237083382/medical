package com.ruoyi.business.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 医疗查询当前有效结果。
 */
public class BizMedicalQueryResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long requestId;
    private String resultSource;
    private String columnSchema;
    private String resultData;
    private String resultSummary;
    private Integer version;
    private String uploadedBy;
    private Date uploadedTime;
    private String updateReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getResultSource() { return resultSource; }
    public void setResultSource(String resultSource) { this.resultSource = resultSource; }
    public String getColumnSchema() { return columnSchema; }
    public void setColumnSchema(String columnSchema) { this.columnSchema = columnSchema; }
    public String getResultData() { return resultData; }
    public void setResultData(String resultData) { this.resultData = resultData; }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public Date getUploadedTime() { return uploadedTime; }
    public void setUploadedTime(Date uploadedTime) { this.uploadedTime = uploadedTime; }
    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
}
