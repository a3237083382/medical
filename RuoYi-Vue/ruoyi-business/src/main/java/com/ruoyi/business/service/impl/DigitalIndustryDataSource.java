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
    @SuppressWarnings("unchecked")
    public Map<String, Object> query(MedicalQueryRequest request)
    {
        if (!health())
        {
            throw new MedicalQueryException("5002", "digital industry data source not configured");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("queryType", request.getQueryType());
        body.put("queryParams", request.getQueryParams());
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

