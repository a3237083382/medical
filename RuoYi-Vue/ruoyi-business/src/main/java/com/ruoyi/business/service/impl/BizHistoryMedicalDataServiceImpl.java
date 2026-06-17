package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizHistoryMedicalData;
import com.ruoyi.business.mapper.BizHistoryMedicalDataMapper;
import com.ruoyi.business.service.IBizHistoryMedicalDataService;

@Service
public class BizHistoryMedicalDataServiceImpl implements IBizHistoryMedicalDataService
{
    @Autowired
    private BizHistoryMedicalDataMapper mapper;

    @Override
    public BizHistoryMedicalData selectById(Long id)
    {
        return mapper.selectBizHistoryMedicalDataById(id);
    }

    @Override
    public List<BizHistoryMedicalData> selectList(BizHistoryMedicalData data)
    {
        return mapper.selectBizHistoryMedicalDataList(data);
    }

    @Override
    public List<String> selectDistinctBatchNo()
    {
        return mapper.selectDistinctBatchNo();
    }

    @Override
    public BizHistoryMedicalData selectByQuery(String queryType, String patientName, String idCard)
    {
        return mapper.selectByQuery(queryType, patientName, idCard);
    }
}
