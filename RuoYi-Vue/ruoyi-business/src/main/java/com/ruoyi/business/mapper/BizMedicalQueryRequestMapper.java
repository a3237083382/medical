package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizMedicalQueryRequest;

public interface BizMedicalQueryRequestMapper
{
    int insertBizMedicalQueryRequest(BizMedicalQueryRequest request);

    BizMedicalQueryRequest selectReusableDelayedRequest(@Param("companyId") Long companyId,
            @Param("patientName") String patientName, @Param("idCard") String idCard);

    BizMedicalQueryRequest selectCompanyRequestByNo(@Param("companyId") Long companyId,
            @Param("requestNo") String requestNo);

    List<BizMedicalQueryRequest> selectCompanyDelayedHistory(BizMedicalQueryRequest request);

    List<BizMedicalQueryRequest> selectCompanyRealtimeHistory(BizMedicalQueryRequest request);

    int countCompanyUnreadDelayedResults(@Param("companyId") Long companyId);

    int markCompanyDelayedRequestRead(@Param("companyId") Long companyId, @Param("requestNo") String requestNo);

    List<BizMedicalQueryRequest> selectDelayedRequestList(BizMedicalQueryRequest request);

    BizMedicalQueryRequest selectDelayedRequestById(Long id);

    BizMedicalQueryRequest selectDelayedRequestByIdForUpdate(Long id);

    int markDelayedProcessing(@Param("id") Long id);

    int completeDelayedRequest(@Param("id") Long id, @Param("resultStatus") String resultStatus,
            @Param("feeSnapshot") java.math.BigDecimal feeSnapshot, @Param("queryLogId") Long queryLogId);

    int markProcessing(@Param("id") Long id);

    int updatePatientName(@Param("id") Long id, @Param("patientName") String patientName);

    int finishRequest(@Param("id") Long id,
            @Param("processStatus") String processStatus,
            @Param("uploadStatus") String uploadStatus,
            @Param("resultStatus") String resultStatus,
            @Param("feeSnapshot") java.math.BigDecimal feeSnapshot,
            @Param("queryLogId") Long queryLogId);
}
