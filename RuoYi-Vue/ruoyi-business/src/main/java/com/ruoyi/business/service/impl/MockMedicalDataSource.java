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
        Map<String, Object> params = request == null ? null : request.getQueryParams();
        String patientName = valueOrDefault(params, "name", "");
        String idCard = valueOrDefault(params, "idCard", "");
        MockMedicalData data;
        try
        {
            data = mockMedicalDataMapper.selectAvailableByQuery(request.getQueryType(), patientName, idCard);
        }
        catch (BadSqlGrammarException e)
        {
            return new LinkedHashMap<>();
        }
        if (data == null)
        {
            return new LinkedHashMap<>();
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
        String queryType = request == null ? null : request.getQueryType();
        Map<String, Object> result = baseResult(params);
        result.put("source", "mock_fallback");
        result.put("template", queryType);
        if ("medical_insurance".equals(queryType))
        {
            result.put("diagnosis", "高血压");
            result.put("summary", "返回医保参保信息和参保至今的就诊记录。");
            result.put("riskLevel", "关注");
            result.put("records", java.util.List.of(
                    record("参保状态", "正常参保", "医保区划：长沙市，单位名称：中南大学"),
                    record("住院记录", "湘雅医院 20251001-20251023", "病种：高血压，报销费用：2000"),
                    record("门诊记录", "湘雅医院 20251201", "病种：高血压，报销费用：100")));
            return result;
        }
        if ("medical_record".equals(queryType))
        {
            result.put("diagnosis", "伤筋");
            result.put("summary", "返回电子病历中的入院记录、出院记录、手术记录和影像报告。");
            result.put("riskLevel", "关注");
            result.put("records", java.util.List.of(
                    record("入院记录", "2022-09-20 涟源中医院骨科", "主诉：腰退疼痛，入院诊断：伤筋"),
                    record("出院记录", "住院 7 天", "诊疗经过：中医+西医，医嘱：定期门诊复查"),
                    record("手术记录", "2022-10-01 半月板修复", "麻醉方法：椎管内麻醉"),
                    record("影像报告", "左膝关节正侧位", "报告医生：练小红")));
            return result;
        }
        if ("medical_order".equals(queryType))
        {
            result.put("diagnosis", "尾骨折");
            result.put("summary", "返回医嘱相关的就诊日期、医院、药品、科室和诊断。");
            result.put("riskLevel", "低风险");
            result.put("records", java.util.List.of(
                    record("浏阳市中医院 针灸科", "红花 1(g)", "就诊日期：20250611，诊断：尾骨折，类型：门诊")));
            return result;
        }
        if ("medical_image".equals(queryType))
        {
            result.put("diagnosis", "影像检查");
            result.put("summary", "返回影像检查报告单信息。");
            result.put("riskLevel", "低风险");
            result.put("records", java.util.List.of(
                    record("左膝关节正侧位", "影像号：1067216", "检查日期：2022-07-22，医院：涟源中医院，科室：骨科，报告医生：练小红")));
            return result;
        }
        if ("medical_surgery".equals(queryType))
        {
            result.put("diagnosis", "半月板撕裂");
            result.put("summary", "返回近期手术记录。");
            result.put("riskLevel", "关注");
            result.put("records", java.util.List.of(
                    record("半月板修复", "2022-10-01", "术前/术中诊断：半月板撕裂，手术者：毛晓东，麻醉者：张旭佳")));
            return result;
        }
        if ("medical_exam".equals(queryType))
        {
            result.put("diagnosis", "体检未见明显异常");
            result.put("summary", "返回体检项目、小结、报告日期、医师和体检医院。");
            result.put("riskLevel", "低风险");
            result.put("records", java.util.List.of(
                    record("总检结果", "身体健康", "模板样例"),
                    record("总检建议", "身体健康，注意清淡饮食", "模板样例"),
                    record("心电图", "未见明显异常", "2025-01-01 湘雅二医院 毛晓东"),
                    record("血常规", "未见明显异常", "2025-01-01 湘雅二医院 毛晓东"),
                    record("B超检查", "未见明显异常", "2025-01-01 湘雅二医院 毛晓东"),
                    record("CT", "未见明显异常", "2025-01-01 湘雅二医院 毛晓东")));
            return result;
        }
        result.put("diagnosis", "高血压");
        result.put("summary", "返回医疗机构、科室、就诊类型、日期和诊断。");
        result.put("riskLevel", "关注");
        result.put("records", java.util.List.of(
                record("湘雅医院 神经内科", "住院/门诊 20251001", "诊断：高血压"),
                record("湘雅医院 神经内科", "住院 20251001-20251007", "诊断：高血压"),
                record("数据处理", "已脱敏", "姓名、身份证和诊断信息按平台规则脱敏展示")));
        return result;
    }

    private Map<String, Object> baseResult(Map<String, Object> params)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("patientName", valueOrDefault(params, "name", "刘亮"));
        result.put("idCard", valueOrDefault(params, "idCard", "432503198706012770"));
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
