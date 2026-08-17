package com.ruoyi.business.domain.medical;

import java.util.ArrayList;
import java.util.List;

public class MedicalQueryBatchSubmission
{
    private String serviceMode;
    private String queryType;
    private List<MedicalQueryBatchRow> rows = new ArrayList<>();

    public String getServiceMode() { return serviceMode; }
    public void setServiceMode(String serviceMode) { this.serviceMode = serviceMode; }
    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }
    public List<MedicalQueryBatchRow> getRows() { return rows; }
    public void setRows(List<MedicalQueryBatchRow> rows) { this.rows = rows; }
}
