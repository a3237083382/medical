package com.ruoyi.business.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ruoyi.business.config.MedicalDataSourceProperties;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.service.MedicalDataSource;
import com.ruoyi.business.service.MedicalQueryException;

@Service
public class DigitalIndustryDataSource implements MedicalDataSource
{
    private final MedicalDataSourceProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public DigitalIndustryDataSource(MedicalDataSourceProperties properties)
    {
        this.properties = properties;
    }

    @Override
    public Map<String, Object> query(MedicalQueryRequest request)
    {
        if (!health())
        {
            throw new MedicalQueryException("5002", "digital industry data source not configured");
        }
        Map<String, Object> body = buildRequestBody(request);
        try
        {
            Map<String, Object> result = restTemplate.postForObject(
                    properties.getDigitalIndustry().getQueryUrl(), body, Map.class);
            return result == null ? new LinkedHashMap<>() : result;
        }
        catch (RestClientException e)
        {
            throw new MedicalQueryException("5003", "digital industry data source request failed");
        }
    }

    private Map<String, Object> buildRequestBody(MedicalQueryRequest request)
    {
        Map<String, Object> body = new LinkedHashMap<>();
        Map<String, Object> params = request == null ? null : request.getQueryParams();
        if (params != null && params.containsKey("sfzhm"))
        {
            body.put("sfzhm", params.get("sfzhm"));
            body.put("startdate", params.get("startdate"));
            body.put("enddate", params.get("enddate"));
        }
        else
        {
            body.put("queryType", request == null ? null : request.getQueryType());
            body.put("queryParams", params);
        }
        return body;
    }

    @Override
    public boolean health()
    {
        return properties.getDigitalIndustry().isEnabled()
                && properties.getDigitalIndustry().getQueryUrl() != null
                && !properties.getDigitalIndustry().getQueryUrl().trim().isEmpty();
    }

    @Override
    public String sourceCode()
    {
        return "digital";
    }
}

