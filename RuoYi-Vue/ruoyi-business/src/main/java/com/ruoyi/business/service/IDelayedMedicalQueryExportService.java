package com.ruoyi.business.service;

import com.ruoyi.business.domain.medical.MedicalQueryExportFile;

public interface IDelayedMedicalQueryExportService
{
    MedicalQueryExportFile exportRequest(Long companyId, String requestNo);

    MedicalQueryExportFile exportBatch(Long companyId, String batchNo);
}
