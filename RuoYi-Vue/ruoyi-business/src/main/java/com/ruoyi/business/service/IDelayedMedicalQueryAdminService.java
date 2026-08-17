package com.ruoyi.business.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.medical.DelayedMedicalQueryBatchDetail;
import com.ruoyi.business.domain.medical.DelayedMedicalQueryAdminDetail;
import com.ruoyi.business.domain.medical.DelayedMedicalQueryResultCommand;

public interface IDelayedMedicalQueryAdminService
{
    List<BizMedicalQueryRequest> selectList(BizMedicalQueryRequest request);

    List<BizMedicalQueryBatch> selectBatchList(BizMedicalQueryBatch batch);

    DelayedMedicalQueryBatchDetail getBatchDetail(Long id);

    DelayedMedicalQueryAdminDetail getDetail(Long id);

    void start(Long id);

    DelayedMedicalQueryResultCommand previewExcel(Long id, MultipartFile file);

    void saveDraft(Long id, DelayedMedicalQueryResultCommand command, String username);

    void complete(Long id, DelayedMedicalQueryResultCommand command, String username);

    void updateUploaded(Long id, DelayedMedicalQueryResultCommand command, String username);
}
