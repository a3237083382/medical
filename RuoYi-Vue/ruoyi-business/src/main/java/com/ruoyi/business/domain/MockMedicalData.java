package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class MockMedicalData extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String queryType;
    private String patientName;
    private String idCard;
    private String diagnosis;
    private String dataJson;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
