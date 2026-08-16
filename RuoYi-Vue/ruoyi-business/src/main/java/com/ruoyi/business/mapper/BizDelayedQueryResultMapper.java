package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizDelayedQueryResult;

public interface BizDelayedQueryResultMapper
{
    public List<BizDelayedQueryResult> selectByRequestId(Long requestId);

    public int deleteByRequestId(Long requestId);

    public int insertBizDelayedQueryResult(BizDelayedQueryResult result);
}
