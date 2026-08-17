package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.BizMedicalQueryResult;

public interface BizMedicalQueryResultMapper
{
    int insertBizMedicalQueryResult(BizMedicalQueryResult result);

    BizMedicalQueryResult selectByRequestId(Long requestId);

    int updateDraft(BizMedicalQueryResult result);

    int uploadResult(BizMedicalQueryResult result);

    int updateUploadedResult(BizMedicalQueryResult result);
}
