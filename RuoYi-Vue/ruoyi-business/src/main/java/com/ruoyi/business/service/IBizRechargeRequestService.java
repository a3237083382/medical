package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizRechargeRequest;

public interface IBizRechargeRequestService
{
    public BizRechargeRequest selectBizRechargeRequestById(Long id);

    public List<BizRechargeRequest> selectBizRechargeRequestList(BizRechargeRequest request);

    public List<BizRechargeRequest> selectByCompanyId(Long companyId);

    public int insertBizRechargeRequest(BizRechargeRequest request);

    public int approve(Long id, String reviewer, String reviewRemark);

    public int reject(Long id, String reviewer, String reviewRemark);
}
