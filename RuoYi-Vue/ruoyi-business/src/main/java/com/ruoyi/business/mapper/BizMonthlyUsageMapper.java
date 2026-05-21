package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizMonthlyUsage;

public interface BizMonthlyUsageMapper
{
    public BizMonthlyUsage selectUsage(@Param("companyId") Long companyId, @Param("billingMonth") String billingMonth);

    public List<BizMonthlyUsage> selectBizMonthlyUsageList(BizMonthlyUsage usage);

    public int ensureUsage(@Param("companyId") Long companyId,
            @Param("billingMonth") String billingMonth,
            @Param("budgetAmount") BigDecimal budgetAmount);

    public int reserveBudget(@Param("companyId") Long companyId,
            @Param("billingMonth") String billingMonth,
            @Param("budgetAmount") BigDecimal budgetAmount,
            @Param("reserveAmount") BigDecimal reserveAmount);

    public int confirmBudget(@Param("companyId") Long companyId,
            @Param("billingMonth") String billingMonth,
            @Param("reserveAmount") BigDecimal reserveAmount,
            @Param("actualAmount") BigDecimal actualAmount);

    public int releaseBudget(@Param("companyId") Long companyId,
            @Param("billingMonth") String billingMonth,
            @Param("reserveAmount") BigDecimal reserveAmount);
}
