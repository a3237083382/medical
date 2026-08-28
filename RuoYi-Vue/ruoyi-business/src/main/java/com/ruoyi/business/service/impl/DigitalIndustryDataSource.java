package com.ruoyi.business.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.Charset;
import com.alibaba.fastjson.JSONObject;
import com.crc.openapi.sdk.common.CommonEnum;
import com.crc.openapi.sdk.entity.Result;
import com.hnchasing.digital.openapi.java.sdk.OpenapiClient;
import com.hnchasing.digital.openapi.java.sdk.config.MisuanProperties;
import com.hnchasing.digital.openapi.java.sdk.config.OpenapiClientProperties;
import com.hnchasing.digital.openapi.java.sdk.config.OpenapiConstants;
import com.hnchasing.digital.openapi.java.sdk.config.OpenapiProjectProperties;
import com.hnchasing.digital.openapi.java.sdk.config.RequestParams;

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
    private volatile OpenapiClient sdkClient;

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
        if (configuredSdk())
        {
            return queryBySdk(request);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        Map<String, Object> params = request.getQueryParams();
        if (params != null && params.containsKey("sfzhm"))
        {
            body.put("sfzhm", params.get("sfzhm"));
            body.put("startdate", params.get("startdate"));
            body.put("enddate", params.get("enddate"));
        }
        else
        {
            body.put("queryType", request.getQueryType());
            body.put("queryParams", params);
        }
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

    private boolean configuredSdk()
    {
        MedicalDataSourceProperties.DigitalIndustry d = properties.getDigitalIndustry();
        return d.getApiId() != null && !d.getApiId().trim().isEmpty()
                && d.getPartnerId() != null && !d.getPartnerId().trim().isEmpty()
                && d.getSignSecret() != null && !d.getSignSecret().trim().isEmpty();
    }

    private Map<String, Object> queryBySdk(MedicalQueryRequest request)
    {
        try
        {
            MedicalDataSourceProperties.DigitalIndustry d = properties.getDigitalIndustry();
            if (sdkClient == null)
            {
                OpenapiClientProperties client = new OpenapiClientProperties();
                client.setOpenapiUrl(d.getQueryUrl());
                client.setEnvironment(OpenapiConstants.EnvironmentEnum.valueOf(d.getEnvironment()));
                client.setPartnerId(d.getPartnerId()); client.setSysId(d.getSysId());
                client.setAppSubId(d.getAppSubId()); client.setAppToken(d.getAppToken());
                client.setMsgSecret(d.getMsgSecret()); client.setSignSecret(d.getSignSecret());
                MisuanProperties misuan = new MisuanProperties();
                misuan.setDataServiceId(d.getDataServiceId()); misuan.setAccessKeyId(d.getAccessKeyId());
                misuan.setAccessKeySecret(d.getAccessKeySecret()); misuan.setSm2PublicKey(d.getSm2PublicKey());
                OpenapiProjectProperties project = new OpenapiProjectProperties();
                project.setGateway(CommonEnum.GatewayTypeEnum.SYS);
                project.setSignMethod(CommonEnum.SignMethodEnum.SM3);
                project.setMisuanProperties(misuan);
                Map<String, OpenapiProjectProperties> projects = new HashMap<>();
                projects.put(d.getApiId(), project);
                sdkClient = new OpenapiClient(client, projects);
            }
            Map<String, Object> p = request.getQueryParams();
            JSONObject body = new JSONObject();
            body.put("sfzhm", p.get("sfzhm")); body.put("startdate", p.get("startdate")); body.put("enddate", p.get("enddate"));
            Map<String, String> headers = new HashMap<>(); headers.put("rest-post-header", "restPostHeader");
            RequestParams rp = RequestParams.builder().bodyVariable(body).headers(headers).build();
            Result result = sdkClient.openapiRestful(d.getApiId(), rp);
            if (result == null || result.getReturnData() == null || result.getReturnData().trim().isEmpty())
            {
                return new LinkedHashMap<>();
            }
            Map<String, Object> parsed = com.alibaba.fastjson2.JSON.parseObject(result.getReturnData(), Map.class);
            repairEncoding(parsed);
            return parsed;
        }
        catch (Exception e)
        {
            throw new MedicalQueryException("5003", "digital industry SDK request failed");
        }
    }

    @SuppressWarnings("unchecked")
    private void repairEncoding(Object value)
    {
        if (value instanceof Map<?, ?> map)
        {
            for (Map.Entry<?, ?> entry : map.entrySet())
            {
                if (entry.getValue() instanceof String text)
                {
                    ((Map<Object, Object>) map).put(entry.getKey(), repairText(text));
                }
                else
                {
                    repairEncoding(entry.getValue());
                }
            }
        }
        else if (value instanceof Iterable<?> iterable)
        {
            for (Object item : iterable)
            {
                repairEncoding(item);
            }
        }
    }

    private String repairText(String text)
    {
        if (text.indexOf('\ufffd') < 0 && !text.matches(".*[寮锟闂缁绛娴鐥].*"))
        {
            return text;
        }
        try
        {
            String candidate = new String(text.getBytes(Charset.forName("GBK")), java.nio.charset.StandardCharsets.UTF_8);
            if (candidate.indexOf('\ufffd') < text.indexOf('\ufffd'))
            {
                return candidate;
            }
        }
        catch (Exception ignored)
        {
            // Keep the original value if the source text is not GBK-mojibake.
        }
        return text;
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

