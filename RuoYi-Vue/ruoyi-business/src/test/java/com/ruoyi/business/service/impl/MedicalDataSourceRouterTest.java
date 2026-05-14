package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ruoyi.business.config.MedicalDataSourceProperties;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.service.MedicalDataSource;
import com.ruoyi.business.service.MedicalQueryException;

class MedicalDataSourceRouterTest
{
    @Test
    void queryUsesConfiguredSourceForQueryType()
    {
        MedicalDataSourceProperties properties = new MedicalDataSourceProperties();
        properties.getRoutes().put("medical_all", "digital");
        MedicalDataSourceRouter router = new MedicalDataSourceRouter(properties,
                List.of(source("mock", true), source("digital", true)));

        Map<String, Object> result = router.query(request("medical_all"));

        assertEquals("digital", result.get("source"));
    }

    @Test
    void queryFallsBackToHealthyDefaultSourceWhenConfiguredSourceIsUnhealthy()
    {
        MedicalDataSourceProperties properties = new MedicalDataSourceProperties();
        properties.setDefaultSource("mock");
        properties.getRoutes().put("medical_all", "digital");
        MedicalDataSourceRouter router = new MedicalDataSourceRouter(properties,
                List.of(source("mock", true), source("digital", false)));

        Map<String, Object> result = router.query(request("medical_all"));

        assertEquals("mock", result.get("source"));
    }

    @Test
    void queryFailsWhenNoHealthySourceExists()
    {
        MedicalDataSourceProperties properties = new MedicalDataSourceProperties();
        MedicalDataSourceRouter router = new MedicalDataSourceRouter(properties, List.of(source("mock", false)));

        MedicalQueryException ex = assertThrows(MedicalQueryException.class, () -> router.query(request("medical_all")));

        assertEquals("5001", ex.getCode());
    }

    private static MedicalQueryRequest request(String queryType)
    {
        MedicalQueryRequest request = new MedicalQueryRequest();
        request.setQueryType(queryType);
        return request;
    }

    private static MedicalDataSource source(String sourceCode, boolean healthy)
    {
        return new MedicalDataSource()
        {
            @Override
            public Map<String, Object> query(MedicalQueryRequest request)
            {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("source", sourceCode);
                return result;
            }

            @Override
            public boolean health()
            {
                return healthy;
            }

            @Override
            public String sourceCode()
            {
                return sourceCode;
            }
        };
    }
}
