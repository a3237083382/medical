package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 医疗查询工作流请求。
 */
public class BizMedicalQueryRequest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String requestNo;
    private Long companyId;
    private String companyName;
    private String entryType;
    private String serviceMode;
    private String queryType;
    private String patientName;
    private String idCard;
    private String processStatus;
    private String uploadStatus;
    private String resultStatus;
    private String viewStatus;
    private Long priceConfigId;
    private BigDecimal reservedFee;
    private BigDecimal feeSnapshot;
    private String billingMonth;
    private Long queryLogId;
    private String requestIp;
    private Date processStartTime;
    private Date completeTime;
    private Integer version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }
    public String getServiceMode() { return serviceMode; }
    public void setServiceMode(String serviceMode) { this.serviceMode = serviceMode; }
    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }
    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    public String getViewStatus() { return viewStatus; }
    public void setViewStatus(String viewStatus) { this.viewStatus = viewStatus; }
    public Long getPriceConfigId() { return priceConfigId; }
    public void setPriceConfigId(Long priceConfigId) { this.priceConfigId = priceConfigId; }
    public BigDecimal getReservedFee() { return reservedFee; }
    public void setReservedFee(BigDecimal reservedFee) { this.reservedFee = reservedFee; }
    public BigDecimal getFeeSnapshot() { return feeSnapshot; }
    public void setFeeSnapshot(BigDecimal feeSnapshot) { this.feeSnapshot = feeSnapshot; }
    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }
    public Long getQueryLogId() { return queryLogId; }
    public void setQueryLogId(Long queryLogId) { this.queryLogId = queryLogId; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }
    public Date getProcessStartTime() { return processStartTime; }
    public void setProcessStartTime(Date processStartTime) { this.processStartTime = processStartTime; }
    public Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(Date completeTime) { this.completeTime = completeTime; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
