package com.ruoyi.business.service;

import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmission;
import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmissionResult;

public interface IMedicalQueryBatchSubmissionService
{
    MedicalQueryBatchSubmissionResult submit(Long companyId, MedicalQueryBatchSubmission command, String requestIp);
}
