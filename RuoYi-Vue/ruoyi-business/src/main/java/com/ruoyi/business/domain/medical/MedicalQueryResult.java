package com.ruoyi.business.domain.medical;

import java.math.BigDecimal;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MedicalQueryResult
{
    private String code = "0";
    private String msg = "success";
    private Long queryId;
    private String requestNo;
    private String processStatus;
    private String uploadStatus;
    private String resultStatus;
    private String serviceStatus;
    private BigDecimal fee;
    @JsonIgnore
    private BigDecimal balanceAfter;
    private Map<String, Object> data;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public Long getQueryId()
    {
        return queryId;
    }

    public void setQueryId(Long queryId)
    {
        this.queryId = queryId;
    }

    public String getRequestNo()
    {
        return requestNo;
    }

    public void setRequestNo(String requestNo)
    {
        this.requestNo = requestNo;
    }

    public String getProcessStatus()
    {
        return processStatus;
    }

    public void setProcessStatus(String processStatus)
    {
        this.processStatus = processStatus;
    }

    public String getUploadStatus()
    {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus)
    {
        this.uploadStatus = uploadStatus;
    }

    public String getResultStatus()
    {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus)
    {
        this.resultStatus = resultStatus;
    }

    public String getServiceStatus()
    {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus)
    {
        this.serviceStatus = serviceStatus;
    }

    public BigDecimal getFee()
    {
        return fee;
    }

    public void setFee(BigDecimal fee)
    {
        this.fee = fee;
    }

    public BigDecimal getBalanceAfter()
    {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter)
    {
        this.balanceAfter = balanceAfter;
    }

    public Map<String, Object> getData()
    {
        return data;
    }

    public void setData(Map<String, Object> data)
    {
        this.data = data;
    }
}
