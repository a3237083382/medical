package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizFeeFlow;

public interface BizFeeFlowMapper
{
    public BizFeeFlow selectBizFeeFlowById(Long id);

    public List<BizFeeFlow> selectBizFeeFlowList(BizFeeFlow flow);

    public int insertBizFeeFlow(BizFeeFlow flow);
}
