package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.business.domain.BizFeeFlow;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.mapper.BizFeeFlowMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizQueryLogMapper;

@Service
public class BillingSettlementService
{
    private static final int DEFAULT_BILLING_CYCLE_DAYS = 30;

    private final BizInsuranceCompanyMapper companyMapper;
    private final BizQueryLogMapper queryLogMapper;
    private final BizFeeFlowMapper feeFlowMapper;

    public BillingSettlementService(BizInsuranceCompanyMapper companyMapper,
            BizQueryLogMapper queryLogMapper, BizFeeFlowMapper feeFlowMapper)
    {
        this.companyMapper = companyMapper;
        this.queryLogMapper = queryLogMapper;
        this.feeFlowMapper = feeFlowMapper;
    }

    @Transactional
    public int settleDueCompanies()
    {
        return settleDueCompanies(DEFAULT_BILLING_CYCLE_DAYS);
    }

    @Transactional
    public int settleDueCompanies(Integer billingCycleDays)
    {
        List<BizInsuranceCompany> companies = companyMapper.selectBizInsuranceCompanyList(new BizInsuranceCompany());
        Date settlementTime = new Date();
        int cycleDays = normalizeBillingCycleDays(billingCycleDays);
        int settledCount = 0;
        for (BizInsuranceCompany company : companies)
        {
            if (company.getId() != null && isDue(company, settlementTime, cycleDays) && settleCompany(company, settlementTime))
            {
                settledCount++;
            }
        }
        return settledCount;
    }

    private boolean settleCompany(BizInsuranceCompany company, Date settlementTime)
    {
        BigDecimal totalFee = queryLogMapper.sumUnsettledSuccessFeeByCompanyId(company.getId(), settlementTime);
        if (totalFee == null)
        {
            totalFee = BigDecimal.ZERO;
        }

        BigDecimal balanceBefore = company.getBalance() == null ? BigDecimal.ZERO : company.getBalance();
        if (totalFee.compareTo(BigDecimal.ZERO) <= 0)
        {
            BizInsuranceCompany update = new BizInsuranceCompany();
            update.setId(company.getId());
            update.setBalanceUpdateTime(settlementTime);
            companyMapper.updateBizInsuranceCompany(update);
            return true;
        }

        BigDecimal balanceAfter = balanceBefore.subtract(totalFee);
        BizFeeFlow flow = new BizFeeFlow();
        flow.setCompanyId(company.getId());
        flow.setOperationType("SETTLEMENT");
        flow.setAmount(totalFee.negate());
        flow.setBalanceBefore(balanceBefore);
        flow.setBalanceAfter(balanceAfter);
        flow.setOperator("SCHEDULE_JOB");
        flow.setOperationTime(settlementTime);
        flow.setRemark("CYCLE_QUERY_FEE");
        feeFlowMapper.insertBizFeeFlow(flow);

        companyMapper.settleBalance(company.getId(), totalFee, settlementTime);
        queryLogMapper.updateSettlementIdForUnsettledSuccessLogs(company.getId(), flow.getId(), settlementTime);
        return true;
    }

    private boolean isDue(BizInsuranceCompany company, Date now, int billingCycleDays)
    {
        Date baseTime = company.getBalanceUpdateTime();
        if (baseTime == null)
        {
            baseTime = company.getCreateTime();
        }
        if (baseTime == null)
        {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseTime);
        calendar.add(Calendar.DAY_OF_MONTH, billingCycleDays);
        return !calendar.getTime().after(now);
    }

    private int normalizeBillingCycleDays(Integer billingCycleDays)
    {
        return billingCycleDays == null || billingCycleDays <= 0 ? DEFAULT_BILLING_CYCLE_DAYS : billingCycleDays;
    }
}
