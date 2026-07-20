package com.ruoyi.business.domain;
import com.ruoyi.common.core.domain.BaseEntity;
public class BizHisBigdata extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long id; private String importBatchNo; private String patientName; private String idCard;
    private String dataCategory; private String dataJson;
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getImportBatchNo() { return importBatchNo; } public void setImportBatchNo(String v) { this.importBatchNo = v; }
    public String getPatientName() { return patientName; } public void setPatientName(String v) { this.patientName = v; }
    public String getIdCard() { return idCard; } public void setIdCard(String v) { this.idCard = v; }
    public String getDataCategory() { return dataCategory; } public void setDataCategory(String v) { this.dataCategory = v; }
    public String getDataJson() { return dataJson; } public void setDataJson(String v) { this.dataJson = v; }
}