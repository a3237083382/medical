package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.ruoyi.business.domain.BizFeeFlow;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMonthlyBill;
import com.ruoyi.business.domain.BizMonthlyBillDetail;
import com.ruoyi.business.mapper.BizFeeFlowMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMonthlyBillDetailMapper;
import com.ruoyi.business.mapper.BizMonthlyBillMapper;
import com.ruoyi.business.mapper.BizQueryLogMapper;

@Service
public class BillingSettlementService
{
    private static final int DEFAULT_BILLING_CYCLE_DAYS = 30;

    private final BizInsuranceCompanyMapper companyMapper;
    private final BizQueryLogMapper queryLogMapper;
    private final BizFeeFlowMapper feeFlowMapper;
    private final BizMonthlyBillMapper monthlyBillMapper;
    private final BizMonthlyBillDetailMapper monthlyBillDetailMapper;

    public BillingSettlementService(BizInsuranceCompanyMapper companyMapper,
            BizQueryLogMapper queryLogMapper, BizFeeFlowMapper feeFlowMapper)
    {
        this(companyMapper, queryLogMapper, feeFlowMapper, null, null);
    }

    @Autowired
    public BillingSettlementService(BizInsuranceCompanyMapper companyMapper,
            BizQueryLogMapper queryLogMapper, BizFeeFlowMapper feeFlowMapper,
            BizMonthlyBillMapper monthlyBillMapper, BizMonthlyBillDetailMapper monthlyBillDetailMapper)
    {
        this.companyMapper = companyMapper;
        this.queryLogMapper = queryLogMapper;
        this.feeFlowMapper = feeFlowMapper;
        this.monthlyBillMapper = monthlyBillMapper;
        this.monthlyBillDetailMapper = monthlyBillDetailMapper;
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

    @Transactional
    public int generateMonthlyBills(String billingMonth)
    {
        if (monthlyBillMapper == null || monthlyBillDetailMapper == null)
        {
            return 0;
        }
        List<BizInsuranceCompany> companies = companyMapper.selectBizInsuranceCompanyList(new BizInsuranceCompany());
        int generatedCount = 0;
        for (BizInsuranceCompany company : companies)
        {
            if (company.getId() != null && generateMonthlyBill(company, billingMonth))
            {
                generatedCount++;
            }
        }
        return generatedCount;
    }

    private boolean generateMonthlyBill(BizInsuranceCompany company, String billingMonth)
    {
        BizMonthlyBillDetail filter = new BizMonthlyBillDetail();
        filter.setCompanyId(company.getId());
        filter.setBillingMonth(billingMonth);
        List<BizMonthlyBillDetail> details = monthlyBillDetailMapper.selectSummaryDetails(filter);
        if (details == null || details.isEmpty())
        {
            monthlyBillMapper.deleteBill(company.getId(), billingMonth);
            return false;
        }

        int queryCount = 0;
        int hitCount = 0;
        int noResultCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (BizMonthlyBillDetail detail : details)
        {
            int count = detail.getQueryCount() == null ? 0 : detail.getQueryCount();
            queryCount += count;
            if ("HIT".equals(detail.getResultStatus()))
            {
                hitCount += count;
            }
            if ("NO_RESULT".equals(detail.getResultStatus()))
            {
                noResultCount += count;
            }
            totalAmount = totalAmount.add(detail.getTotalAmount() == null ? BigDecimal.ZERO : detail.getTotalAmount());
        }

        BizMonthlyBill existing = monthlyBillMapper.selectBill(company.getId(), billingMonth);
        if (existing != null && existing.getId() != null)
        {
            monthlyBillMapper.deleteBillDetails(existing.getId());
        }
        monthlyBillMapper.deleteBill(company.getId(), billingMonth);

        BizMonthlyBill bill = new BizMonthlyBill();
        bill.setCompanyId(company.getId());
        bill.setBillingMonth(billingMonth);
        bill.setQueryCount(queryCount);
        bill.setHitCount(hitCount);
        bill.setNoResultCount(noResultCount);
        bill.setTotalAmount(totalAmount);
        bill.setStatus("0");
        bill.setGeneratedTime(new Date());
        monthlyBillMapper.insertBizMonthlyBill(bill);

        for (BizMonthlyBillDetail detail : details)
        {
            detail.setBillId(bill.getId());
            monthlyBillDetailMapper.insertBizMonthlyBillDetail(detail);
        }
        return true;
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
