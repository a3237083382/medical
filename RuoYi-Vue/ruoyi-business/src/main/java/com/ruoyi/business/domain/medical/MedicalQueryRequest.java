package com.ruoyi.business.domain.medical;

import java.util.Map;

public class MedicalQueryRequest
{
    private Long companyId;
    private String queryType;
    private Map<String, Object> queryParams;
    private String requestIp;

    public Long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(Long companyId)
    {
        this.companyId = companyId;
    }

    public String getQueryType()
    {
        return queryType;
    }

    public void setQueryType(String queryType)
    {
        this.queryType = queryType;
    }

    public Map<String, Object> getQueryParams()
    {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams)
    {
        this.queryParams = queryParams;
    }

    public String getRequestIp()
    {
        return requestIp;
    }

    public void setRequestIp(String requestIp)
    {
        this.requestIp = requestIp;
    }
}
