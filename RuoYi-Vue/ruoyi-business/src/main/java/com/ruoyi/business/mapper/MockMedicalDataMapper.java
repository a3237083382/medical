package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.MockMedicalData;

public interface MockMedicalDataMapper
{
    MockMedicalData selectAvailableByQueryType(String queryType);
}
