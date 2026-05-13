package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizQueryLog;

public interface BizQueryLogMapper
{
    public BizQueryLog selectBizQueryLogById(Long id);

    public List<BizQueryLog> selectBizQueryLogList(BizQueryLog queryLog);

    public int insertBizQueryLog(BizQueryLog queryLog);
}
