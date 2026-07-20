package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.BizHisClinicVisit;
import com.ruoyi.business.domain.BizHisHospitalization;
import com.ruoyi.business.domain.BizHisBigdata;
import com.ruoyi.business.domain.BizHisAttachment;

public interface IBizHistoryQueryService
{
    Map<String, Object> queryByPerson(String patientName, String idCard);

    List<BizHisClinicVisit> queryClinicVisits(String patientName, String idCard);

    List<BizHisHospitalization> queryHospitalizations(String patientName, String idCard);

    List<BizHisBigdata> queryBigdata(String patientName, String idCard);

    List<BizHisAttachment> queryAttachments(String patientName, String idCard);

    /**
     * 将实时查询结果缓存到历史数据库。
     * 如果该患者已有相同内容的记录则跳过。
     * @param queryType 查询类型
     * @param rawData 原始数据（脱敏前）
     */
    void cacheQueryResult(String queryType, java.util.Map<String, Object> rawData);
}
