package com.ruoyi.business.mapper;
import java.util.List;
import com.ruoyi.business.domain.BizHisBigdata;
public interface BizHisBigdataMapper {
    List<BizHisBigdata> selectByPerson(String patientName, String idCard);
    int insertBatch(List<BizHisBigdata> list);
    int deleteBizHisBigdataByBatchNo(String batchNo);
    String selectLatestDataJsonByPerson(String patientName, String idCard);
}