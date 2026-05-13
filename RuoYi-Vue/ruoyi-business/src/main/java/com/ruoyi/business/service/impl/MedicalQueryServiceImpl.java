package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizFeeFlow;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.domain.medical.MedicalQueryResult;
import com.ruoyi.business.mapper.BizFeeFlowMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizQueryLogMapper;
import com.ruoyi.business.mapper.BizQueryPriceMapper;
import com.ruoyi.business.service.IMedicalQueryService;
import com.ruoyi.business.service.MedicalDataSource;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.business.util.DesensitizeUtil;

@Service
public class MedicalQueryServiceImpl implements IMedicalQueryService
{
    private final BizInsuranceCompanyMapper companyMapper;
    private final BizQueryPriceMapper priceMapper;
    private final BizQueryLogMapper queryLogMapper;
    private final BizFeeFlowMapper feeFlowMapper;
    private final MedicalDataSource medicalDataSource;

    public MedicalQueryServiceImpl(BizInsuranceCompanyMapper companyMapper, BizQueryPriceMapper priceMapper,
            BizQueryLogMapper queryLogMapper, BizFeeFlowMapper feeFlowMapper, MedicalDataSource medicalDataSource)
    {
        this.companyMapper = companyMapper;
        this.priceMapper = priceMapper;
        this.queryLogMapper = queryLogMapper;
        this.feeFlowMapper = feeFlowMapper;
        this.medicalDataSource = medicalDataSource;
    }

    @Override
    @Transactional
    public MedicalQueryResult query(MedicalQueryRequest request)
    {
        validate(request);
        BizInsuranceCompany before = companyMapper.selectBizInsuranceCompanyById(request.getCompanyId());
        if (before == null || !"0".equals(before.getStatus()))
        {
            throw new MedicalQueryException("4002", "company disabled or not found");
        }
        BigDecimal balanceBefore = before.getBalance();
        BigDecimal fee = getQueryPrice(request.getQueryType());
        if (companyMapper.deductBalance(request.getCompanyId(), fee) == 0)
        {
            throw new MedicalQueryException("4001", "insufficient balance");
        }

        Map<String, Object> data = DesensitizeUtil.desensitize(medicalDataSource.query(request));
        BizInsuranceCompany after = companyMapper.selectBizInsuranceCompanyById(request.getCompanyId());
        BizQueryLog log = insertQueryLog(request, fee);
        insertFeeFlow(request.getCompanyId(), fee, balanceBefore, after.getBalance(), log.getId());

        MedicalQueryResult result = new MedicalQueryResult();
        result.setFee(fee);
        result.setBalanceAfter(after.getBalance());
        result.setData(data);
        return result;
    }

    public BigDecimal getBalance(Long companyId)
    {
        BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyById(companyId);
        if (company == null)
        {
            throw new MedicalQueryException("4002", "company not found");
        }
        return company.getBalance();
    }

    public BigDecimal getQueryPrice(String queryType)
    {
        BizQueryPrice price = priceMapper.selectBizQueryPriceByQueryType(queryType);
        if (price == null || !"0".equals(price.getStatus()))
        {
            throw new MedicalQueryException("4003", "query price disabled or not found");
        }
        return price.getFee();
    }

    private void validate(MedicalQueryRequest request)
    {
        if (request == null || request.getCompanyId() == null)
        {
            throw new MedicalQueryException("4000", "companyId is required");
        }
        if (request.getQueryType() == null || request.getQueryType().isEmpty())
        {
            throw new MedicalQueryException("4000", "queryType is required");
        }
    }

    private BizQueryLog insertQueryLog(MedicalQueryRequest request, BigDecimal fee)
    {
        BizQueryLog log = new BizQueryLog();
        log.setCompanyId(request.getCompanyId());
        log.setQueryType(request.getQueryType());
        log.setQueryParams(JSON.toJSONString(request.getQueryParams()));
        log.setFee(fee);
        log.setStatus("0");
        log.setRequestIp(request.getRequestIp());
        log.setRequestTime(new Date());
        queryLogMapper.insertBizQueryLog(log);
        return log;
    }

    private void insertFeeFlow(Long companyId, BigDecimal fee, BigDecimal balanceBefore, BigDecimal balanceAfter, Long logId)
    {
        BizFeeFlow flow = new BizFeeFlow();
        flow.setCompanyId(companyId);
        flow.setOperationType("DEDUCT");
        flow.setAmount(fee);
        flow.setBalanceBefore(balanceBefore);
        flow.setBalanceAfter(balanceAfter);
        flow.setOperator("external-api");
        flow.setBizId(logId);
        flow.setOperationTime(new Date());
        feeFlowMapper.insertBizFeeFlow(flow);
    }
}
