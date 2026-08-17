package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizDelayedQueryRequest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String requestNo;
    private String batchNo;
    private Long companyId;
    private String companyNameSnapshot;
    private String patientName;
    private String idCard;
    private String queryType;
    private String queryStatus;
    private String uploadStatus;
    private String resultStatus;
    private String resultMessage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;
    private String requestIp;
    private Long handlerId;
    private String handlerName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handledTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadedTime;
    private BigDecimal fee;
    private BigDecimal reservedFee;
    private String billingMonth;
    private String chargedFlag;
    private Long priceConfigId;
    private String modifyBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;
    private String modifyReason;
    private List<BizDelayedQueryResult> results = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyNameSnapshot() { return companyNameSnapshot; }
    public void setCompanyNameSnapshot(String companyNameSnapshot) { this.companyNameSnapshot = companyNameSnapshot; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public String getQueryStatus() { return queryStatus; }
    public void setQueryStatus(String queryStatus) { this.queryStatus = queryStatus; }

    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }

    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }

    public Long getHandlerId() { return handlerId; }
    public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }

    public String getHandlerName() { return handlerName; }
    public void setHandlerName(String handlerName) { this.handlerName = handlerName; }

    public Date getHandledTime() { return handledTime; }
    public void setHandledTime(Date handledTime) { this.handledTime = handledTime; }

    public Date getUploadedTime() { return uploadedTime; }
    public void setUploadedTime(Date uploadedTime) { this.uploadedTime = uploadedTime; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public BigDecimal getReservedFee() { return reservedFee; }
    public void setReservedFee(BigDecimal reservedFee) { this.reservedFee = reservedFee; }

    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public String getChargedFlag() { return chargedFlag; }
    public void setChargedFlag(String chargedFlag) { this.chargedFlag = chargedFlag; }

    public Long getPriceConfigId() { return priceConfigId; }
    public void setPriceConfigId(Long priceConfigId) { this.priceConfigId = priceConfigId; }

    public String getModifyBy() { return modifyBy; }
    public void setModifyBy(String modifyBy) { this.modifyBy = modifyBy; }

    public Date getModifyTime() { return modifyTime; }
    public void setModifyTime(Date modifyTime) { this.modifyTime = modifyTime; }

    public String getModifyReason() { return modifyReason; }
    public void setModifyReason(String modifyReason) { this.modifyReason = modifyReason; }

    public List<BizDelayedQueryResult> getResults() { return results; }
    public void setResults(List<BizDelayedQueryResult> results) { this.results = results == null ? new ArrayList<>() : results; }
}
