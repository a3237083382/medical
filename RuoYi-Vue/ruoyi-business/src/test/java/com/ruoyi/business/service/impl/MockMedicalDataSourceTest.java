package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ruoyi.business.domain.MockMedicalData;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.mapper.MockMedicalDataMapper;

class MockMedicalDataSourceTest
{
    @Test
    void realtimeBigDataQueryUsesIdCardAndReturnsRawVisitRecords()
    {
        MockMedicalDataMapper mapper = mock(MockMedicalDataMapper.class);
        MockMedicalData data = new MockMedicalData();
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("res", List.of(Map.of("sfzhm", "320683198312120713", "jzlx", "门诊")));
        data.setPatientName("张三B");
        data.setIdCard("320683198312120713");
        data.setDataJson(com.alibaba.fastjson2.JSON.toJSONString(raw));
        when(mapper.selectAvailableByIdCard("medical_all", "320683198312120713")).thenReturn(data);

        MedicalQueryRequest request = new MedicalQueryRequest();
        request.setQueryType("medical_all");
        request.setQueryParams(Map.of(
                "sfzhm", "320683198312120713",
                "startdate", "2020-01-01 00:00:00",
                "enddate", "2026-08-29 23:59:59"));

        Map<String, Object> result = new MockMedicalDataSource(mapper).query(request);

        assertEquals("张三B", result.get("patientName"));
        assertEquals("320683198312120713", result.get("idCard"));
        assertEquals(raw.get("res"), result.get("res"));
        verify(mapper).selectAvailableByIdCard(eq("medical_all"), eq("320683198312120713"));
    }
}
