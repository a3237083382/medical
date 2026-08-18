package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizDelayedQueryRequest;

public interface BizDelayedQueryRequestMapper
{
    public BizDelayedQueryRequest selectBizDelayedQueryRequestById(Long id);

    public BizDelayedQueryRequest selectPendingDuplicate(@Param("companyId") Long companyId,
            @Param("patientName") String patientName, @Param("idCard") String idCard,
            @Param("queryType") String queryType);

    public List<BizDelayedQueryRequest> selectBizDelayedQueryRequestList(BizDelayedQueryRequest request);

    public int countPendingRequests();

    public int insertBizDelayedQueryRequest(BizDelayedQueryRequest request);

    public int updateBizDelayedQueryRequest(BizDelayedQueryRequest request);

    public int cancelPendingRequest(@Param("id") Long id, @Param("companyId") Long companyId);
}
