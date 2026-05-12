package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizFeeFlow;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizRechargeRequest;
import com.ruoyi.business.mapper.BizFeeFlowMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizRechargeRequestMapper;
import com.ruoyi.business.service.IBizRechargeRequestService;

@Service
public class BizRechargeRequestServiceImpl implements IBizRechargeRequestService
{
    @Autowired
    private BizRechargeRequestMapper rechargeRequestMapper;

    @Autowired
    private BizInsuranceCompanyMapper companyMapper;

    @Autowired
    private BizFeeFlowMapper feeFlowMapper;

    @Override
    public BizRechargeRequest selectBizRechargeRequestById(Long id)
    {
        return rechargeRequestMapper.selectBizRechargeRequestById(id);
    }

    @Override
    public List<BizRechargeRequest> selectBizRechargeRequestList(BizRechargeRequest request)
    {
        return rechargeRequestMapper.selectBizRechargeRequestList(request);
    }

    @Override
    public List<BizRechargeRequest> selectByCompanyId(Long companyId)
    {
        return rechargeRequestMapper.selectBizRechargeRequestByCompanyId(companyId);
    }

    @Override
    public int insertBizRechargeRequest(BizRechargeRequest request)
    {
        return rechargeRequestMapper.insertBizRechargeRequest(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approve(Long id, String reviewer, String reviewRemark)
    {
        BizRechargeRequest req = rechargeRequestMapper.selectBizRechargeRequestById(id);
        if (req == null || !"0".equals(req.getStatus()))
        {
            throw new RuntimeException("充值申请不存在或已被处理");
        }

        // 原子加余额
        companyMapper.addBalance(req.getCompanyId(), req.getAmount());

        // 查询最新余额用于流水
        BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyById(req.getCompanyId());

        // 记录费用流水
        BizFeeFlow flow = new BizFeeFlow();
        flow.setCompanyId(req.getCompanyId());
        flow.setOperationType("RECHARGE");
        flow.setAmount(req.getAmount());
        flow.setBalanceBefore(company.getBalance().subtract(req.getAmount()));
        flow.setBalanceAfter(company.getBalance());
        flow.setOperator(reviewer);
        flow.setBizId(id);
        flow.setRemark("充值审核通过: " + reviewRemark);
        feeFlowMapper.insertBizFeeFlow(flow);

        // 更新申请状态
        BizRechargeRequest update = new BizRechargeRequest();
        update.setId(id);
        update.setStatus("1");
        update.setReviewTime(new Date());
        update.setReviewer(reviewer);
        update.setReviewRemark(reviewRemark);
        update.setFeeFlowId(flow.getId());
        return rechargeRequestMapper.updateBizRechargeRequest(update);
    }

    @Override
    public int reject(Long id, String reviewer, String reviewRemark)
    {
        BizRechargeRequest req = rechargeRequestMapper.selectBizRechargeRequestById(id);
        if (req == null || !"0".equals(req.getStatus()))
        {
            throw new RuntimeException("充值申请不存在或已被处理");
        }

        BizRechargeRequest update = new BizRechargeRequest();
        update.setId(id);
        update.setStatus("2");
        update.setReviewTime(new Date());
        update.setReviewer(reviewer);
        update.setReviewRemark(reviewRemark);
        return rechargeRequestMapper.updateBizRechargeRequest(update);
    }
}
