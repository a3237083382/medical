package com.ruoyi.business.domain.medical;

import java.util.List;
import java.util.Map;

public class DelayedMedicalQueryResultCommand
{
    private String resultStatus;
    private List<Map<String, Object>> columnSchema;
    private Map<String, Object> data;
    private String resultSummary;
    private String updateReason;

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    public List<Map<String, Object>> getColumnSchema() { return columnSchema; }
    public void setColumnSchema(List<Map<String, Object>> columnSchema) { this.columnSchema = columnSchema; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
}
