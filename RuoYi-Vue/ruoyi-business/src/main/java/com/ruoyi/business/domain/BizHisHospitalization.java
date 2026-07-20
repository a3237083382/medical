package com.ruoyi.business.domain;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date; import java.math.BigDecimal;
public class BizHisHospitalization extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long id; private String importBatchNo; private String patientName; private String idCard;
    private String insuranceType; private String insuranceName; private String medicalCategory; private String medicalCategoryName;
    private String payLocation; private String orgCode; private String orgName; private String orgLevel;
    private Date visitStartTime; private Date visitEndTime; private Date settlementTime;
    private BigDecimal totalFee; private BigDecimal selfPayFull; private BigDecimal excessLimitSelfPay;
    private BigDecimal advanceSelfPay; private BigDecimal withinRangeFee; private BigDecimal deductibleStandard;
    private BigDecimal currentDeductible; private BigDecimal actualDeductible; private BigDecimal poolFundPayment;
    private BigDecimal basicMedicalPayRatio; private BigDecimal civilServantSubsidy; private BigDecimal supplementInsurancePayment;
    private BigDecimal seriousIllnessInsurance; private BigDecimal largeMedicalSubsidy; private BigDecimal disabledPersonnelFund;
    private BigDecimal medicalAssistanceFund; private BigDecimal otherFundPayment; private BigDecimal fundTotalPayment;
    private BigDecimal personalPayment; private BigDecimal personalAccountPayment; private BigDecimal cashPayment;
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getImportBatchNo() { return importBatchNo; } public void setImportBatchNo(String v) { this.importBatchNo = v; }
    public String getPatientName() { return patientName; } public void setPatientName(String v) { this.patientName = v; }
    public String getIdCard() { return idCard; } public void setIdCard(String v) { this.idCard = v; }
    public String getInsuranceType() { return insuranceType; } public void setInsuranceType(String v) { this.insuranceType = v; }
    public String getInsuranceName() { return insuranceName; } public void setInsuranceName(String v) { this.insuranceName = v; }
    public String getMedicalCategory() { return medicalCategory; } public void setMedicalCategory(String v) { this.medicalCategory = v; }
    public String getMedicalCategoryName() { return medicalCategoryName; } public void setMedicalCategoryName(String v) { this.medicalCategoryName = v; }
    public String getPayLocation() { return payLocation; } public void setPayLocation(String v) { this.payLocation = v; }
    public String getOrgCode() { return orgCode; } public void setOrgCode(String v) { this.orgCode = v; }
    public String getOrgName() { return orgName; } public void setOrgName(String v) { this.orgName = v; }
    public String getOrgLevel() { return orgLevel; } public void setOrgLevel(String v) { this.orgLevel = v; }
    public Date getVisitStartTime() { return visitStartTime; } public void setVisitStartTime(Date v) { this.visitStartTime = v; }
    public Date getVisitEndTime() { return visitEndTime; } public void setVisitEndTime(Date v) { this.visitEndTime = v; }
    public Date getSettlementTime() { return settlementTime; } public void setSettlementTime(Date v) { this.settlementTime = v; }
    public BigDecimal getTotalFee() { return totalFee; } public void setTotalFee(BigDecimal v) { this.totalFee = v; }
    public BigDecimal getSelfPayFull() { return selfPayFull; } public void setSelfPayFull(BigDecimal v) { this.selfPayFull = v; }
    public BigDecimal getExcessLimitSelfPay() { return excessLimitSelfPay; } public void setExcessLimitSelfPay(BigDecimal v) { this.excessLimitSelfPay = v; }
    public BigDecimal getAdvanceSelfPay() { return advanceSelfPay; } public void setAdvanceSelfPay(BigDecimal v) { this.advanceSelfPay = v; }
    public BigDecimal getWithinRangeFee() { return withinRangeFee; } public void setWithinRangeFee(BigDecimal v) { this.withinRangeFee = v; }
    public BigDecimal getDeductibleStandard() { return deductibleStandard; } public void setDeductibleStandard(BigDecimal v) { this.deductibleStandard = v; }
    public BigDecimal getCurrentDeductible() { return currentDeductible; } public void setCurrentDeductible(BigDecimal v) { this.currentDeductible = v; }
    public BigDecimal getActualDeductible() { return actualDeductible; } public void setActualDeductible(BigDecimal v) { this.actualDeductible = v; }
    public BigDecimal getPoolFundPayment() { return poolFundPayment; } public void setPoolFundPayment(BigDecimal v) { this.poolFundPayment = v; }
    public BigDecimal getBasicMedicalPayRatio() { return basicMedicalPayRatio; } public void setBasicMedicalPayRatio(BigDecimal v) { this.basicMedicalPayRatio = v; }
    public BigDecimal getCivilServantSubsidy() { return civilServantSubsidy; } public void setCivilServantSubsidy(BigDecimal v) { this.civilServantSubsidy = v; }
    public BigDecimal getSupplementInsurancePayment() { return supplementInsurancePayment; } public void setSupplementInsurancePayment(BigDecimal v) { this.supplementInsurancePayment = v; }
    public BigDecimal getSeriousIllnessInsurance() { return seriousIllnessInsurance; } public void setSeriousIllnessInsurance(BigDecimal v) { this.seriousIllnessInsurance = v; }
    public BigDecimal getLargeMedicalSubsidy() { return largeMedicalSubsidy; } public void setLargeMedicalSubsidy(BigDecimal v) { this.largeMedicalSubsidy = v; }
    public BigDecimal getDisabledPersonnelFund() { return disabledPersonnelFund; } public void setDisabledPersonnelFund(BigDecimal v) { this.disabledPersonnelFund = v; }
    public BigDecimal getMedicalAssistanceFund() { return medicalAssistanceFund; } public void setMedicalAssistanceFund(BigDecimal v) { this.medicalAssistanceFund = v; }
    public BigDecimal getOtherFundPayment() { return otherFundPayment; } public void setOtherFundPayment(BigDecimal v) { this.otherFundPayment = v; }
    public BigDecimal getFundTotalPayment() { return fundTotalPayment; } public void setFundTotalPayment(BigDecimal v) { this.fundTotalPayment = v; }
    public BigDecimal getPersonalPayment() { return personalPayment; } public void setPersonalPayment(BigDecimal v) { this.personalPayment = v; }
    public BigDecimal getPersonalAccountPayment() { return personalAccountPayment; } public void setPersonalAccountPayment(BigDecimal v) { this.personalAccountPayment = v; }
    public BigDecimal getCashPayment() { return cashPayment; } public void setCashPayment(BigDecimal v) { this.cashPayment = v; }
}