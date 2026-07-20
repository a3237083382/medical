package com.ruoyi.business.mapper;
import java.util.List;
import com.ruoyi.business.domain.BizHisHospitalization;
public interface BizHisHospitalizationMapper {
    BizHisHospitalization selectBizHisHospitalizationById(Long id);
    List<BizHisHospitalization> selectBizHisHospitalizationList(BizHisHospitalization data);
    int insertBizHisHospitalization(BizHisHospitalization data);
    int insertBatch(List<BizHisHospitalization> list);
    int deleteBizHisHospitalizationByBatchNo(String batchNo);
    List<BizHisHospitalization> selectByPerson(String patientName, String idCard);
}