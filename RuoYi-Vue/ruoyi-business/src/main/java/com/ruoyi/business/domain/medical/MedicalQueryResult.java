package com.ruoyi.business.domain.medical;

import java.math.BigDecimal;
import java.util.Map;

public class MedicalQueryResult
{
    private String code = "0";
    private String msg = "success";
    private BigDecimal fee;
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
