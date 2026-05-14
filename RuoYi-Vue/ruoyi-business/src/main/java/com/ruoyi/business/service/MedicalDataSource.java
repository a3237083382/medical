package com.ruoyi.business.service;

import java.util.Map;

import com.ruoyi.business.domain.medical.MedicalQueryRequest;

public interface MedicalDataSource
{
    Map<String, Object> query(MedicalQueryRequest request);

    boolean health();

    default String sourceCode()
    {
        return "mock";
    }
}
