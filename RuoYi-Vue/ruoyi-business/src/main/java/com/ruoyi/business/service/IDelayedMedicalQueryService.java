package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.medical.MedicalQueryBatchProgress;
import com.ruoyi.business.domain.medical.MedicalQueryRequestDetail;
import com.ruoyi.business.domain.medical.DelayedMedicalQuerySubmission;
import com.ruoyi.business.domain.medical.MedicalQueryRequestDetail;

public interface IDelayedMedicalQueryService
{
    DelayedMedicalQuerySubmission submit(Long companyId, String patientName, String idCard, String requestIp);

    MedicalQueryRequestDetail getRequest(Long companyId, String requestNo);

    List<MedicalQueryRequestDetail> listHistory(Long companyId, String requestNo, String patientName,
            String processStatus, String resultStatus, String beginTime, String endTime);

    List<MedicalQueryBatchProgress> listBatchHistory(Long companyId);

    int countUnread(Long companyId);

    void markRead(Long companyId, String requestNo);
}
