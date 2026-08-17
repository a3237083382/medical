package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ruoyi.business.domain.BizFeeFlow;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMonthlyBill;
import com.ruoyi.business.domain.BizMonthlyBillDetail;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.mapper.BizFeeFlowMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMonthlyBillDetailMapper;
import com.ruoyi.business.mapper.BizMonthlyBillMapper;
import com.ruoyi.business.mapper.BizQueryLogMapper;

public class BillingSettlementServiceTest
{
    @Test
    public void generateMonthlyBillsSummarizesNaturalMonthByQueryTypeAndResultStatus()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "100.00", daysAgo(31), 30));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper(new BigDecimal("0.00"));
        FakeFeeFlowMapper feeFlowMapper = new FakeFeeFlowMapper();
        FakeMonthlyBillMapper billMapper = new FakeMonthlyBillMapper();
        FakeMonthlyBillDetailMapper detailMapper = new FakeMonthlyBillDetailMapper();
        detailMapper.summaryDetails.add(detail("medical_all", "医疗大数据", "HIT", 2, "40.00"));
        detailMapper.summaryDetails.add(detail("medical_exam", "体检信息", "NO_RESULT", 1, "3.00"));
        BillingSettlementService service = new BillingSettlementService(
                companyMapper, queryLogMapper, feeFlowMapper, billMapper, detailMapper);

        int count = service.generateMonthlyBills("2026-05");

        assertEquals(1, count);
        assertEquals(1, billMapper.bills.size());
        BizMonthlyBill bill = billMapper.bills.get(0);
        assertEquals("2026-05", bill.getBillingMonth());
        assertEquals(Integer.valueOf(3), bill.getQueryCount());
        assertEquals(Integer.valueOf(2), bill.getHitCount());
        assertEquals(Integer.valueOf(1), bill.getNoResultCount());
        assertEquals(new BigDecimal("43.00"), bill.getTotalAmount());
        assertEquals(2, detailMapper.insertedDetails.size());
        assertEquals(Long.valueOf(1L), detailMapper.insertedDetails.get(0).getBillId());
    }

    @Test
    public void settleDueCompaniesDeductsUnsettledFeesAndMarksLogs()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "100.00", daysAgo(31), 30));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper(new BigDecimal("80.00"));
        FakeFeeFlowMapper feeFlowMapper = new FakeFeeFlowMapper();
        BillingSettlementService service = new BillingSettlementService(companyMapper, queryLogMapper, feeFlowMapper);

        int count = service.settleDueCompanies();

        assertEquals(1, count);
        assertEquals(new BigDecimal("20.00"), companyMapper.company.getBalance());
        assertNotNull(companyMapper.company.getBalanceUpdateTime());
        assertEquals(1, feeFlowMapper.flows.size());
        BizFeeFlow flow = feeFlowMapper.flows.get(0);
        assertEquals("SETTLEMENT", flow.getOperationType());
        assertEquals(new BigDecimal("-80.00"), flow.getAmount());
        assertEquals(new BigDecimal("100.00"), flow.getBalanceBefore());
        assertEquals(new BigDecimal("20.00"), flow.getBalanceAfter());
        assertEquals(Long.valueOf(1L), queryLogMapper.settlementId);
    }

    @Test
    public void settleDueCompaniesSkipsCompanyBeforeCycleEnd()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "100.00", daysAgo(5), 30));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper(new BigDecimal("80.00"));
        FakeFeeFlowMapper feeFlowMapper = new FakeFeeFlowMapper();
        BillingSettlementService service = new BillingSettlementService(companyMapper, queryLogMapper, feeFlowMapper);

        int count = service.settleDueCompanies();

        assertEquals(0, count);
        assertEquals(new BigDecimal("100.00"), companyMapper.company.getBalance());
        assertTrue(feeFlowMapper.flows.isEmpty());
        assertEquals(null, queryLogMapper.settlementId);
    }

    @Test
    public void settleDueCompaniesUsesTaskCycleForAllCompanies()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "100.00", daysAgo(10), 7));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper(new BigDecimal("80.00"));
        FakeFeeFlowMapper feeFlowMapper = new FakeFeeFlowMapper();
        BillingSettlementService service = new BillingSettlementService(companyMapper, queryLogMapper, feeFlowMapper);

        int count = service.settleDueCompanies(14);

        assertEquals(0, count);
        assertEquals(new BigDecimal("100.00"), companyMapper.company.getBalance());
        assertTrue(feeFlowMapper.flows.isEmpty());
        assertEquals(null, queryLogMapper.settlementId);
    }

    @Test
    public void settleDueCompaniesSettlesByTaskCycleEvenWhenCompanyCycleDiffers()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "100.00", daysAgo(15), 30));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper(new BigDecimal("80.00"));
        FakeFeeFlowMapper feeFlowMapper = new FakeFeeFlowMapper();
        BillingSettlementService service = new BillingSettlementService(companyMapper, queryLogMapper, feeFlowMapper);

        int count = service.settleDueCompanies(14);

        assertEquals(1, count);
        assertEquals(new BigDecimal("20.00"), companyMapper.company.getBalance());
        assertEquals(1, feeFlowMapper.flows.size());
        assertEquals(Long.valueOf(1L), queryLogMapper.settlementId);
    }

    private static BizInsuranceCompany company(Long id, String balance, Date balanceUpdateTime, int cycleDays)
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(id);
        company.setBalance(new BigDecimal(balance));
        company.setBalanceUpdateTime(balanceUpdateTime);
        company.setBillingCycleDays(cycleDays);
        company.setStatus("0");
        return company;
    }

    private static Date daysAgo(int days)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }

    private static BizMonthlyBillDetail detail(String queryType, String queryName,
            String resultStatus, int queryCount, String totalAmount)
    {
        BizMonthlyBillDetail detail = new BizMonthlyBillDetail();
        detail.setCompanyId(1L);
        detail.setBillingMonth("2026-05");
        detail.setQueryType(queryType);
        detail.setQueryName(queryName);
        detail.setResultStatus(resultStatus);
        detail.setQueryCount(queryCount);
        detail.setTotalAmount(new BigDecimal(totalAmount));
        return detail;
    }

    private static class FakeCompanyMapper implements BizInsuranceCompanyMapper
    {
        private final BizInsuranceCompany company;

        FakeCompanyMapper(BizInsuranceCompany company)
        {
            this.company = company;
        }

        @Override
        public List<BizInsuranceCompany> selectBizInsuranceCompanyList(BizInsuranceCompany query)
        {
            List<BizInsuranceCompany> companies = new ArrayList<>();
            companies.add(company);
            return companies;
        }

        @Override
        public int settleBalance(Long companyId, BigDecimal amount, Date balanceUpdateTime)
        {
            company.setBalance(company.getBalance().subtract(amount));
            company.setBalanceUpdateTime(balanceUpdateTime);
            return 1;
        }

        @Override
        public int updateBizInsuranceCompany(BizInsuranceCompany update)
        {
            company.setBalanceUpdateTime(update.getBalanceUpdateTime());
            return 1;
        }

        @Override public BizInsuranceCompany selectBizInsuranceCompanyById(Long id) { return company; }
        @Override public BizInsuranceCompany selectBizInsuranceCompanyByIdForUpdate(Long id) { return company; }
        @Override public BizInsuranceCompany selectBizInsuranceCompanyByAppKey(String appKey) { return null; }
        @Override public BizInsuranceCompany selectBizInsuranceCompanyByUsername(String username) { return null; }
        @Override public int updateBizInsuranceCompanyLoginInfo(BizInsuranceCompany company) { return 0; }
        @Override public int addBalance(Long companyId, BigDecimal amount) { return 0; }
        @Override public int deductBalance(Long companyId, BigDecimal amount) { return 0; }
        @Override public int insertBizInsuranceCompany(BizInsuranceCompany company) { return 0; }
        @Override public int deleteBizInsuranceCompanyByIds(Long[] ids) { return 0; }
    }

    private static class FakeQueryLogMapper implements BizQueryLogMapper
    {
        private final BigDecimal unsettledFee;
        private Long settlementId;

        FakeQueryLogMapper(BigDecimal unsettledFee)
        {
            this.unsettledFee = unsettledFee;
        }

        @Override
        public BigDecimal sumUnsettledSuccessFeeByCompanyId(Long companyId, Date cutoffTime)
        {
            return unsettledFee;
        }

        @Override
        public int updateSettlementIdForUnsettledSuccessLogs(Long companyId, Long settlementId, Date cutoffTime)
        {
            this.settlementId = settlementId;
            return 1;
        }

        @Override public BizQueryLog selectBizQueryLogById(Long id) { return null; }
        @Override public List<BizQueryLog> selectBizQueryLogList(BizQueryLog queryLog) { return new ArrayList<>(); }
        @Override public int insertBizQueryLog(BizQueryLog queryLog) { return 0; }
    }

    private static class FakeFeeFlowMapper implements BizFeeFlowMapper
    {
        private final List<BizFeeFlow> flows = new ArrayList<>();

        @Override
        public int insertBizFeeFlow(BizFeeFlow flow)
        {
            flow.setId((long) flows.size() + 1);
            flows.add(flow);
            return 1;
        }

        @Override public BizFeeFlow selectBizFeeFlowById(Long id) { return null; }
        @Override public List<BizFeeFlow> selectBizFeeFlowList(BizFeeFlow flow) { return flows; }
    }

    private static class FakeMonthlyBillMapper implements BizMonthlyBillMapper
    {
        private final List<BizMonthlyBill> bills = new ArrayList<>();

        @Override
        public int insertBizMonthlyBill(BizMonthlyBill bill)
        {
            bill.setId((long) bills.size() + 1);
            bills.add(bill);
            return 1;
        }

        @Override public BizMonthlyBill selectBill(Long companyId, String billingMonth) { return null; }
        @Override public BizMonthlyBill selectBizMonthlyBillById(Long id) { return null; }
        @Override public List<BizMonthlyBill> selectBizMonthlyBillList(BizMonthlyBill bill) { return bills; }
        @Override public int deleteBillDetails(Long billId) { return 0; }
        @Override public int deleteBill(Long companyId, String billingMonth) { return 0; }
    }

    private static class FakeMonthlyBillDetailMapper implements BizMonthlyBillDetailMapper
    {
        private final List<BizMonthlyBillDetail> summaryDetails = new ArrayList<>();
        private final List<BizMonthlyBillDetail> insertedDetails = new ArrayList<>();

        @Override public List<BizMonthlyBillDetail> selectBizMonthlyBillDetailList(BizMonthlyBillDetail detail) { return insertedDetails; }
        @Override public List<BizMonthlyBillDetail> selectSummaryDetails(BizMonthlyBillDetail detail) { return summaryDetails; }
        @Override public int insertBizMonthlyBillDetail(BizMonthlyBillDetail detail) { insertedDetails.add(detail); return 1; }
    }
}
