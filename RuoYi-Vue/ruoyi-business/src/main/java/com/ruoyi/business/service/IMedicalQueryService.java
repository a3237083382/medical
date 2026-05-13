package com.ruoyi.business.service;

import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.domain.medical.MedicalQueryResult;

public interface IMedicalQueryService
{
    MedicalQueryResult query(MedicalQueryRequest request);
}
