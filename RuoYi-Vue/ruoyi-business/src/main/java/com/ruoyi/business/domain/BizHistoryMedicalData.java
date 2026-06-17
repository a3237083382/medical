package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 鍘嗗彶鍖荤枟鏁版嵁瀵硅薄 biz_history_medical_data
 */
public class BizHistoryMedicalData extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String importBatchNo;
    private String queryType;
    private String patientName;
    private String idCard;
    private String gender;
    private String birthDate;
    private String phone;
    private String diagnosis;
    private String hospital;
    private String department;
    private String visitDate;
    private String visitType;
    private String diseaseCode;
    private String medicalRecordNo;
    private String doctor;
    private String dataJson;
    private String status;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getImportBatchNo() { return importBatchNo; }
    public void setImportBatchNo(String importBatchNo) { this.importBatchNo = importBatchNo; }
    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }
    public String getDiseaseCode() { return diseaseCode; }
    public void setDiseaseCode(String diseaseCode) { this.diseaseCode = diseaseCode; }
    public String getMedicalRecordNo() { return medicalRecordNo; }
    public void setMedicalRecordNo(String medicalRecordNo) { this.medicalRecordNo = medicalRecordNo; }
    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Override
    public String getRemark() { return remark; }
    @Override
    public void setRemark(String remark) { this.remark = remark; }
}