package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizFeeFlow;

public interface IBizFeeFlowService
{
    public BizFeeFlow selectBizFeeFlowById(Long id);

    public List<BizFeeFlow> selectBizFeeFlowList(BizFeeFlow flow);

    public int insertBizFeeFlow(BizFeeFlow flow);
}
