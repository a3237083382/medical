package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizQueryLog;

public interface IBizQueryLogService
{
    public BizQueryLog selectBizQueryLogById(Long id);

    public List<BizQueryLog> selectBizQueryLogList(BizQueryLog queryLog);

    public int insertBizQueryLog(BizQueryLog queryLog);
}
