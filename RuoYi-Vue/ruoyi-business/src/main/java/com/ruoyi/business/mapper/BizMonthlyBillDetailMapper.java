package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizMonthlyBillDetail;

public interface BizMonthlyBillDetailMapper
{
    public List<BizMonthlyBillDetail> selectBizMonthlyBillDetailList(BizMonthlyBillDetail detail);

    public List<BizMonthlyBillDetail> selectSummaryDetails(BizMonthlyBillDetail detail);

    public int insertBizMonthlyBillDetail(BizMonthlyBillDetail detail);
}
