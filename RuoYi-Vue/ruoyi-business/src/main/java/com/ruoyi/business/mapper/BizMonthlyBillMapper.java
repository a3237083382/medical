package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizMonthlyBill;

public interface BizMonthlyBillMapper
{
    public BizMonthlyBill selectBill(@Param("companyId") Long companyId, @Param("billingMonth") String billingMonth);

    public BizMonthlyBill selectBizMonthlyBillById(Long id);

    public List<BizMonthlyBill> selectBizMonthlyBillList(BizMonthlyBill bill);

    public int deleteBillDetails(@Param("billId") Long billId);

    public int deleteBill(@Param("companyId") Long companyId, @Param("billingMonth") String billingMonth);

    public int insertBizMonthlyBill(BizMonthlyBill bill);
}
