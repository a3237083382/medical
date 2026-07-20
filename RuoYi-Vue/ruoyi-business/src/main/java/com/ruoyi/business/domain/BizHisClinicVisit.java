package com.ruoyi.business.domain;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
public class BizHisClinicVisit extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long id; private String importBatchNo; private String patientName; private String idCard;
    private Date visitTime; private String patientNo; private String insuranceType; private String validFlag;
    private String orgCode; private String orgName; private String medicalRecordNo; private String diseaseName;
    private String outpatientDiagnosis; private String doctorName; private String inpatientDiagnosis;
    private Date endTime; private String conditionDesc;
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getImportBatchNo() { return importBatchNo; } public void setImportBatchNo(String v) { this.importBatchNo = v; }
    public String getPatientName() { return patientName; } public void setPatientName(String v) { this.patientName = v; }
    public String getIdCard() { return idCard; } public void setIdCard(String v) { this.idCard = v; }
    public Date getVisitTime() { return visitTime; } public void setVisitTime(Date v) { this.visitTime = v; }
    public String getPatientNo() { return patientNo; } public void setPatientNo(String v) { this.patientNo = v; }
    public String getInsuranceType() { return insuranceType; } public void setInsuranceType(String v) { this.insuranceType = v; }
    public String getValidFlag() { return validFlag; } public void setValidFlag(String v) { this.validFlag = v; }
    public String getOrgCode() { return orgCode; } public void setOrgCode(String v) { this.orgCode = v; }
    public String getOrgName() { return orgName; } public void setOrgName(String v) { this.orgName = v; }
    public String getMedicalRecordNo() { return medicalRecordNo; } public void setMedicalRecordNo(String v) { this.medicalRecordNo = v; }
    public String getDiseaseName() { return diseaseName; } public void setDiseaseName(String v) { this.diseaseName = v; }
    public String getOutpatientDiagnosis() { return outpatientDiagnosis; } public void setOutpatientDiagnosis(String v) { this.outpatientDiagnosis = v; }
    public String getDoctorName() { return doctorName; } public void setDoctorName(String v) { this.doctorName = v; }
    public String getInpatientDiagnosis() { return inpatientDiagnosis; } public void setInpatientDiagnosis(String v) { this.inpatientDiagnosis = v; }
    public Date getEndTime() { return endTime; } public void setEndTime(Date v) { this.endTime = v; }
    public String getConditionDesc() { return conditionDesc; } public void setConditionDesc(String v) { this.conditionDesc = v; }
}