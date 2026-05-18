package com.ruoyi.business.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.BadSqlGrammarException;
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
        MockMedicalData data;
        try
        {
            data = mockMedicalDataMapper.selectAvailableByQueryType(request.getQueryType());
        }
        catch (BadSqlGrammarException e)
        {
            return buildFallbackData(request);
        }
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

    private Map<String, Object> buildFallbackData(MedicalQueryRequest request)
    {
        Map<String, Object> params = request == null ? null : request.getQueryParams();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientName", valueOrDefault(params, "name", "张三"));
        result.put("idCard", valueOrDefault(params, "idCard", "430102199001018888"));
        result.put("diagnosis", "样例普通门诊记录");
        result.put("summary", "未发现样例高风险医疗记录。当前为 mock 表缺失时的演示数据。");
        result.put("riskLevel", "低风险");
        result.put("records", java.util.List.of(
                record("医疗风险汇总", "低风险", "样例数据未命中重大疾病、长期住院或高频就诊记录"),
                record("最近就诊", "2026-04-18", "普通门诊，样例返回"),
                record("数据处理", "已脱敏", "姓名、身份证和诊断信息按平台规则脱敏展示")));
        return result;
    }

    private String valueOrDefault(Map<String, Object> params, String key, String defaultValue)
    {
        if (params == null || params.get(key) == null)
        {
            return defaultValue;
        }
        String value = String.valueOf(params.get(key)).trim();
        return value.isEmpty() ? defaultValue : value;
    }

    private Map<String, Object> record(String name, String value, String remark)
    {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("name", name);
        record.put("value", value);
        record.put("remark", remark);
        return record;
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
