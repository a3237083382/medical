package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.MockMedicalData;
import org.apache.ibatis.annotations.Param;

public interface MockMedicalDataMapper
{
    MockMedicalData selectAvailableByQueryType(String queryType);

    MockMedicalData selectAvailableByQuery(@Param("queryType") String queryType,
            @Param("patientName") String patientName, @Param("idCard") String idCard);
}
