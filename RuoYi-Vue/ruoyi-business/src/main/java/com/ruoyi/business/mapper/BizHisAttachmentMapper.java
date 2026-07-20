package com.ruoyi.business.mapper;
import java.util.List;
import com.ruoyi.business.domain.BizHisAttachment;
public interface BizHisAttachmentMapper {
    List<BizHisAttachment> selectByPerson(String patientName, String idCard);
    int insertBatch(List<BizHisAttachment> list);
    int deleteBizHisAttachmentByBatchNo(String batchNo);
}