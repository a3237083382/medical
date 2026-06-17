package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizHistoryMedicalData;

public interface BizHistoryMedicalDataMapper
{
    BizHistoryMedicalData selectBizHistoryMedicalDataById(Long id);

    List<BizHistoryMedicalData> selectBizHistoryMedicalDataList(BizHistoryMedicalData data);

    int insertBizHistoryMedicalData(BizHistoryMedicalData data);

    int insertBatch(List<BizHistoryMedicalData> list);

    int deleteBizHistoryMedicalDataByBatchNo(String batchNo);

    List<String> selectDistinctBatchNo();

    BizHistoryMedicalData selectByQuery(String queryType, String patientName, String idCard);
}