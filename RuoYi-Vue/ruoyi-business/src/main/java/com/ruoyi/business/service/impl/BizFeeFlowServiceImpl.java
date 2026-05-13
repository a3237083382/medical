package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizFeeFlow;
import com.ruoyi.business.mapper.BizFeeFlowMapper;
import com.ruoyi.business.service.IBizFeeFlowService;

@Service
public class BizFeeFlowServiceImpl implements IBizFeeFlowService
{
    @Autowired
    private BizFeeFlowMapper feeFlowMapper;

    @Override
    public BizFeeFlow selectBizFeeFlowById(Long id)
    {
        return feeFlowMapper.selectBizFeeFlowById(id);
    }

    @Override
    public List<BizFeeFlow> selectBizFeeFlowList(BizFeeFlow flow)
    {
        return feeFlowMapper.selectBizFeeFlowList(flow);
    }

    @Override
    public int insertBizFeeFlow(BizFeeFlow flow)
    {
        return feeFlowMapper.insertBizFeeFlow(flow);
    }
}
