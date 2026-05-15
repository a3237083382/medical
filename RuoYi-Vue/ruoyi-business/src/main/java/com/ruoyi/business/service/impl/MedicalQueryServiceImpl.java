package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.domain.medical.MedicalQueryResult;
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
    private final MedicalDataSource medicalDataSource;

    public MedicalQueryServiceImpl(BizInsuranceCompanyMapper companyMapper, BizQueryPriceMapper priceMapper,
            BizQueryLogMapper queryLogMapper, MedicalDataSource medicalDataSource)
    {
        this.companyMapper = companyMapper;
        this.priceMapper = priceMapper;
        this.queryLogMapper = queryLogMapper;
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
        if (isNegative(before.getBalance()))
        {
            throw new MedicalQueryException("4001", "insufficient balance");
        }
        BigDecimal fee = getQueryPrice(request.getQueryType());

        Map<String, Object> data = DesensitizeUtil.desensitize(medicalDataSource.query(request));
        insertQueryLog(request, fee);

        MedicalQueryResult result = new MedicalQueryResult();
        result.setFee(fee);
        result.setBalanceAfter(before.getBalance());
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

    private boolean isNegative(BigDecimal value)
    {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }
}
