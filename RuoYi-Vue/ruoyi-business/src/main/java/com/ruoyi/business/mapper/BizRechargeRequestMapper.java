package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizRechargeRequest;

public interface BizRechargeRequestMapper
{
    public BizRechargeRequest selectBizRechargeRequestById(Long id);

    public List<BizRechargeRequest> selectBizRechargeRequestList(BizRechargeRequest request);

    public List<BizRechargeRequest> selectBizRechargeRequestByCompanyId(Long companyId);

    public int insertBizRechargeRequest(BizRechargeRequest request);

    public int updateBizRechargeRequest(BizRechargeRequest request);
}
