package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizDelayedQueryRequest;

public interface BizDelayedQueryRequestMapper
{
    public BizDelayedQueryRequest selectBizDelayedQueryRequestById(Long id);

    public BizDelayedQueryRequest selectPendingDuplicate(@Param("companyId") Long companyId,
            @Param("patientName") String patientName, @Param("idCard") String idCard);

    public List<BizDelayedQueryRequest> selectBizDelayedQueryRequestList(BizDelayedQueryRequest request);

    public int insertBizDelayedQueryRequest(BizDelayedQueryRequest request);

    public int updateBizDelayedQueryRequest(BizDelayedQueryRequest request);
}
