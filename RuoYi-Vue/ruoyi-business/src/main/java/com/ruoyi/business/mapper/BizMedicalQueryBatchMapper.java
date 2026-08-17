package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.BizMedicalQueryBatchItem;
import com.ruoyi.business.domain.medical.MedicalQueryBatchItemState;
import com.ruoyi.business.domain.medical.MedicalQueryBatchProgress;

public interface BizMedicalQueryBatchMapper
{
    int insertBizMedicalQueryBatch(BizMedicalQueryBatch batch);

    int insertBizMedicalQueryBatchItem(BizMedicalQueryBatchItem item);

    List<BizMedicalQueryBatch> selectDelayedBatchList(BizMedicalQueryBatch batch);

    BizMedicalQueryBatch selectDelayedBatchById(Long id);

    List<BizMedicalQueryBatchItem> selectDelayedBatchItems(Long batchId);

    BizMedicalQueryBatch selectCompanyBatchByNoForUpdate(@Param("companyId") Long companyId,
            @Param("batchNo") String batchNo);

    BizMedicalQueryBatch selectCompanyBatchByNo(@Param("companyId") Long companyId,
            @Param("batchNo") String batchNo);

    List<MedicalQueryBatchItemState> selectBatchItemsForUpdate(Long batchId);

    MedicalQueryBatchItemState selectCompanyBatchItemForUpdate(@Param("companyId") Long companyId,
            @Param("itemId") Long itemId);

    int cancelBatchItem(Long itemId);

    int countActiveItemsByRequestId(Long requestId);

    int cancelPendingRequest(Long requestId);

    MedicalQueryBatchProgress selectCompanyBatchProgress(@Param("companyId") Long companyId,
            @Param("batchNo") String batchNo);

    List<MedicalQueryBatchProgress> selectCompanyBatchHistory(@Param("companyId") Long companyId);

    int updateBatchSummary(MedicalQueryBatchProgress progress);
}
