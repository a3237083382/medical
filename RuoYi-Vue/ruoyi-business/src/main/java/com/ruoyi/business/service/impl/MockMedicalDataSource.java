package com.ruoyi.business.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.business.domain.MockMedicalData;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.mapper.MockMedicalDataMapper;
import com.ruoyi.business.service.MedicalDataSource;
import com.ruoyi.business.service.MedicalQueryException;

@Service
public class MockMedicalDataSource implements MedicalDataSource
{
    private final MockMedicalDataMapper mockMedicalDataMapper;

    public MockMedicalDataSource(MockMedicalDataMapper mockMedicalDataMapper)
    {
        this.mockMedicalDataMapper = mockMedicalDataMapper;
    }

    @Override
    public Map<String, Object> query(MedicalQueryRequest request)
    {
        MockMedicalData data = mockMedicalDataMapper.selectAvailableByQueryType(request.getQueryType());
        if (data == null)
        {
            throw new MedicalQueryException("4004", "medical data not found");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientName", data.getPatientName());
        result.put("idCard", data.getIdCard());
        result.put("diagnosis", data.getDiagnosis());
        if (data.getDataJson() != null && !data.getDataJson().isEmpty())
        {
            result.putAll(JSON.parseObject(data.getDataJson(), new TypeReference<Map<String, Object>>() {}));
        }
        return result;
    }

    @Override
    public boolean health()
    {
        return true;
    }

    @Override
    public String sourceCode()
    {
        return "mock";
    }
}
