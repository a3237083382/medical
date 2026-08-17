package com.ruoyi.business.service;

import com.ruoyi.business.domain.medical.MedicalQueryBatchCancellationResult;
import com.ruoyi.business.domain.medical.MedicalQueryBatchProgress;

public interface IMedicalQueryBatchCancellationService
{
    MedicalQueryBatchProgress getProgress(Long companyId, String batchNo);

    MedicalQueryBatchCancellationResult cancelBatch(Long companyId, String batchNo);

    MedicalQueryBatchCancellationResult cancelItem(Long companyId, Long itemId);
}
