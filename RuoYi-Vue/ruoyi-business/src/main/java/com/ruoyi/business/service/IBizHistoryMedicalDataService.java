package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizHistoryMedicalData;

public interface IBizHistoryMedicalDataService
{
    BizHistoryMedicalData selectById(Long id);

    List<BizHistoryMedicalData> selectList(BizHistoryMedicalData data);

    List<String> selectDistinctBatchNo();

    BizHistoryMedicalData selectByQuery(String queryType, String patientName, String idCard);
}
