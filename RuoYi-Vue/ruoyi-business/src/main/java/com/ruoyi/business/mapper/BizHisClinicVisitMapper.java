package com.ruoyi.business.mapper;
import java.util.List;
import com.ruoyi.business.domain.BizHisClinicVisit;
public interface BizHisClinicVisitMapper {
    BizHisClinicVisit selectBizHisClinicVisitById(Long id);
    List<BizHisClinicVisit> selectBizHisClinicVisitList(BizHisClinicVisit data);
    int insertBizHisClinicVisit(BizHisClinicVisit data);
    int insertBatch(List<BizHisClinicVisit> list);
    int deleteBizHisClinicVisitByBatchNo(String batchNo);
    List<BizHisClinicVisit> selectByPerson(String patientName, String idCard);
}