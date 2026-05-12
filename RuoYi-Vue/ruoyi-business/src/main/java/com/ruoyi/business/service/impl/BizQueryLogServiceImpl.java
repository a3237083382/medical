package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.mapper.BizQueryLogMapper;
import com.ruoyi.business.service.IBizQueryLogService;

@Service
public class BizQueryLogServiceImpl implements IBizQueryLogService
{
    @Autowired
    private BizQueryLogMapper queryLogMapper;

    @Override
    public BizQueryLog selectBizQueryLogById(Long id)
    {
        return queryLogMapper.selectBizQueryLogById(id);
    }

    @Override
    public List<BizQueryLog> selectBizQueryLogList(BizQueryLog queryLog)
    {
        return queryLogMapper.selectBizQueryLogList(queryLog);
    }
}
