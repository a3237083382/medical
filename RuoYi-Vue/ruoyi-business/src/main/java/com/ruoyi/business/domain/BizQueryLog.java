package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 查询日志对象 biz_query_log
 */
public class BizQueryLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private String requestNo;

    private String batchNo;

    private String serviceMode;

    private String entryType;

    private Long companyId;

    private String companyName;

    @Excel(name = "查询类型")
    private String queryType;

    private String queryParams;

    @Excel(name = "费用")
    private BigDecimal fee;

    private String billingMonth;

    private String resultStatus;

    private BigDecimal feeSnapshot;

    private Long priceConfigId;

    private Long settlementId;

    @Excel(name = "状态", readConverterExp = "0=成功,1=失败")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "请求时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date requestTime;

    private String requestIp;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getServiceMode() { return serviceMode; }
    public void setServiceMode(String serviceMode) { this.serviceMode = serviceMode; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public String getQueryParams() { return queryParams; }
    public void setQueryParams(String queryParams) { this.queryParams = queryParams; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public BigDecimal getFeeSnapshot() { return feeSnapshot; }
    public void setFeeSnapshot(BigDecimal feeSnapshot) { this.feeSnapshot = feeSnapshot; }

    public Long getPriceConfigId() { return priceConfigId; }
    public void setPriceConfigId(Long priceConfigId) { this.priceConfigId = priceConfigId; }

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getRequestTime() { return requestTime; }
    public void setRequestTime(Date requestTime) { this.requestTime = requestTime; }

    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }
}
