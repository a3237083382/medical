package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.domain.medical.MedicalQueryResult;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
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
    private final BizCompanyQueryPriceMapper companyPriceMapper;
    private final BizMonthlyUsageMapper monthlyUsageMapper;

    @Autowired
    public MedicalQueryServiceImpl(BizInsuranceCompanyMapper companyMapper, BizQueryPriceMapper priceMapper,
            BizQueryLogMapper queryLogMapper, MedicalDataSource medicalDataSource,
            BizCompanyQueryPriceMapper companyPriceMapper, BizMonthlyUsageMapper monthlyUsageMapper)
    {
        this.companyMapper = companyMapper;
        this.priceMapper = priceMapper;
        this.queryLogMapper = queryLogMapper;
        this.medicalDataSource = medicalDataSource;
        this.companyPriceMapper = companyPriceMapper;
        this.monthlyUsageMapper = monthlyUsageMapper;
    }

    public MedicalQueryServiceImpl(BizInsuranceCompanyMapper companyMapper, BizQueryPriceMapper priceMapper,
            BizQueryLogMapper queryLogMapper, MedicalDataSource medicalDataSource)
    {
        this(companyMapper, priceMapper, queryLogMapper, medicalDataSource, null, null);
    }

    @Override
    @Transactional
    public MedicalQueryResult query(MedicalQueryRequest request)
    {
        validate(request);
        BizInsuranceCompany company = companyMapper.selectBizInsuranceCompanyById(request.getCompanyId());
        if (company == null || !"0".equals(company.getStatus()))
        {
            throw new MedicalQueryException("4002", "company disabled or not found");
        }

        QueryPriceSnapshot price = resolveQueryPrice(request.getCompanyId(), request.getQueryType());
        BigDecimal reserveAmount = price.maxFee();
        BigDecimal balanceBefore = company.getBalance() == null ? BigDecimal.ZERO : company.getBalance();
        String billingMonth = currentBillingMonth();
        boolean monthlyBudgetEnabled = isMonthlyBudgetEnabled(company);
        if (monthlyBudgetEnabled)
        {
            reserveMonthlyBudget(company, billingMonth, reserveAmount);
        }
        else if (balanceBefore.compareTo(reserveAmount) < 0)
        {
            throw new MedicalQueryException("4001", "insufficient balance");
        }

        Map<String, Object> data;
        try
        {
            data = DesensitizeUtil.desensitize(medicalDataSource.query(request));
        }
        catch (RuntimeException e)
        {
            if (monthlyBudgetEnabled)
            {
                monthlyUsageMapper.releaseBudget(request.getCompanyId(), billingMonth, reserveAmount);
            }
            throw e;
        }

        String resultStatus = isNoResult(data) ? "NO_RESULT" : "HIT";
        BigDecimal actualFee = "NO_RESULT".equals(resultStatus) ? price.noResultFee : price.hitFee;
        if (monthlyBudgetEnabled)
        {
            monthlyUsageMapper.confirmBudget(request.getCompanyId(), billingMonth, reserveAmount, actualFee);
        }
        BizQueryLog log = insertQueryLog(request, actualFee, billingMonth, resultStatus, price.priceConfigId);

        MedicalQueryResult result = new MedicalQueryResult();
        result.setQueryId(log.getId());
        result.setResultStatus(resultStatus);
        result.setServiceStatus("MONTHLY_SERVICE_ACTIVE");
        result.setFee(actualFee);
        result.setBalanceAfter(balanceBefore);
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

    private QueryPriceSnapshot resolveQueryPrice(Long companyId, String queryType)
    {
        if (companyPriceMapper != null)
        {
            BizCompanyQueryPrice price = companyPriceMapper.selectActivePrice(companyId, queryType);
            if (price != null)
            {
                return new QueryPriceSnapshot(price.getId(), nvl(price.getHitFee()), nvl(price.getNoResultFee()));
            }
        }
        BigDecimal fee = getQueryPrice(queryType);
        return new QueryPriceSnapshot(null, fee, fee);
    }

    private void reserveMonthlyBudget(BizInsuranceCompany company, String billingMonth, BigDecimal reserveAmount)
    {
        BigDecimal monthlyBudget = nvl(company.getMonthlyBudget());
        monthlyUsageMapper.ensureUsage(company.getId(), billingMonth, monthlyBudget);
        int reserved = monthlyUsageMapper.reserveBudget(company.getId(), billingMonth, monthlyBudget, reserveAmount);
        if (reserved <= 0)
        {
            throw new MedicalQueryException("4001", "本月服务额度已达上限");
        }
    }

    private boolean isMonthlyBudgetEnabled(BizInsuranceCompany company)
    {
        return monthlyUsageMapper != null && "0".equals(company.getBudgetEnabled())
                && company.getMonthlyBudget() != null;
    }

    private String currentBillingMonth()
    {
        return YearMonth.now().toString();
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isNoResult(Map<String, Object> data)
    {
        if (data == null || data.isEmpty())
        {
            return true;
        }
        Object records = data.get("records");
        if (records instanceof Iterable<?> iterable)
        {
            return !iterable.iterator().hasNext();
        }
        return false;
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

    private BizQueryLog insertQueryLog(MedicalQueryRequest request, BigDecimal fee,
            String billingMonth, String resultStatus, Long priceConfigId)
    {
        BizQueryLog log = new BizQueryLog();
        log.setCompanyId(request.getCompanyId());
        log.setQueryType(request.getQueryType());
        log.setQueryParams(JSON.toJSONString(request.getQueryParams()));
        log.setFee(fee);
        log.setBillingMonth(billingMonth);
        log.setResultStatus(resultStatus);
        log.setFeeSnapshot(fee);
        log.setPriceConfigId(priceConfigId);
        log.setStatus("0");
        log.setRequestIp(request.getRequestIp());
        log.setRequestTime(new Date());
        queryLogMapper.insertBizQueryLog(log);
        return log;
    }

    private static class QueryPriceSnapshot
    {
        private final Long priceConfigId;
        private final BigDecimal hitFee;
        private final BigDecimal noResultFee;

        private QueryPriceSnapshot(Long priceConfigId, BigDecimal hitFee, BigDecimal noResultFee)
        {
            this.priceConfigId = priceConfigId;
            this.hitFee = hitFee;
            this.noResultFee = noResultFee;
        }

        private BigDecimal maxFee()
        {
            return hitFee.compareTo(noResultFee) >= 0 ? hitFee : noResultFee;
        }
    }
}
