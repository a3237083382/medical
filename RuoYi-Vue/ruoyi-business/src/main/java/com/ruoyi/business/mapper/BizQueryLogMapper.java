package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizQueryLog;

public interface BizQueryLogMapper
{
    public BizQueryLog selectBizQueryLogById(Long id);

    public List<BizQueryLog> selectBizQueryLogList(BizQueryLog queryLog);

    public int insertBizQueryLog(BizQueryLog queryLog);

    public BigDecimal sumUnsettledSuccessFeeByCompanyId(@Param("companyId") Long companyId,
            @Param("cutoffTime") Date cutoffTime);

    public int updateSettlementIdForUnsettledSuccessLogs(@Param("companyId") Long companyId,
            @Param("settlementId") Long settlementId,
            @Param("cutoffTime") Date cutoffTime);
}
