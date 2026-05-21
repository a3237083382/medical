package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

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
import com.ruoyi.business.service.MedicalDataSource;
import com.ruoyi.business.service.MedicalQueryException;

public class MedicalQueryServiceImplTest
{
    @Test
    public void queryUsesCompanySpecificHitAndNoResultPricesWithMonthlyBudget()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "100.00"));
        companyMapper.company.setMonthlyBudget(new BigDecimal("100.00"));
        companyMapper.company.setBudgetEnabled("0");
        FakePriceMapper priceMapper = new FakePriceMapper(price("medical_all", "50.00"));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        FakeMedicalDataSource dataSource = new FakeMedicalDataSource();
        FakeCompanyQueryPriceMapper companyPriceMapper = new FakeCompanyQueryPriceMapper(1L, "medical_all", "20.00", "3.00");
        FakeMonthlyUsageMapper monthlyUsageMapper = new FakeMonthlyUsageMapper();
        MedicalQueryServiceImpl service = new MedicalQueryServiceImpl(
                companyMapper, priceMapper, queryLogMapper, dataSource, companyPriceMapper, monthlyUsageMapper);

        MedicalQueryRequest hitRequest = request(1L, "medical_all");
        MedicalQueryResult hitResult = service.query(hitRequest);

        assertEquals("HIT", hitResult.getResultStatus());
        assertEquals(Long.valueOf(1L), hitResult.getQueryId());
        assertEquals(new BigDecimal("20.00"), monthlyUsageMapper.usedAmount);
        assertEquals(new BigDecimal("0.00"), monthlyUsageMapper.reservedAmount);
        assertEquals(new BigDecimal("20.00"), queryLogMapper.logs.get(0).getFeeSnapshot());
        assertEquals("HIT", queryLogMapper.logs.get(0).getResultStatus());
        assertEquals(Long.valueOf(1L), queryLogMapper.logs.get(0).getPriceConfigId());

        dataSource.emptyResult = true;
        MedicalQueryResult noResult = service.query(request(1L, "medical_all"));

        assertEquals("NO_RESULT", noResult.getResultStatus());
        assertEquals(new BigDecimal("23.00"), monthlyUsageMapper.usedAmount);
        assertEquals(new BigDecimal("3.00"), queryLogMapper.logs.get(1).getFeeSnapshot());
        assertEquals("NO_RESULT", queryLogMapper.logs.get(1).getResultStatus());
    }

    @Test
    public void queryRejectsBeforeCallingDataSourceWhenMonthlyBudgetCannotReserveMaxFee()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "100.00"));
        companyMapper.company.setMonthlyBudget(new BigDecimal("25.00"));
        companyMapper.company.setBudgetEnabled("0");
        FakePriceMapper priceMapper = new FakePriceMapper(price("medical_all", "50.00"));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        FakeMedicalDataSource dataSource = new FakeMedicalDataSource();
        FakeCompanyQueryPriceMapper companyPriceMapper = new FakeCompanyQueryPriceMapper(1L, "medical_all", "20.00", "3.00");
        FakeMonthlyUsageMapper monthlyUsageMapper = new FakeMonthlyUsageMapper();
        monthlyUsageMapper.usedAmount = new BigDecimal("10.00");
        monthlyUsageMapper.reservedAmount = new BigDecimal("0.00");
        MedicalQueryServiceImpl service = new MedicalQueryServiceImpl(
                companyMapper, priceMapper, queryLogMapper, dataSource, companyPriceMapper, monthlyUsageMapper);

        try
        {
            service.query(request(1L, "medical_all"));
            fail("Expected monthly budget error");
        }
        catch (MedicalQueryException ex)
        {
            assertEquals("4001", ex.getCode());
            assertEquals("本月服务额度已达上限", ex.getMessage());
        }

        assertEquals(0, dataSource.queryCount);
        assertTrue(queryLogMapper.logs.isEmpty());
        assertEquals(new BigDecimal("10.00"), monthlyUsageMapper.usedAmount);
    }

    @Test
    public void queryWritesLogWithoutDeductingBalanceBeforeSettlement()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "100.00"));
        FakePriceMapper priceMapper = new FakePriceMapper(price("medical_all", "50.00"));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        FakeFeeFlowMapper feeFlowMapper = new FakeFeeFlowMapper();
        FakeMedicalDataSource dataSource = new FakeMedicalDataSource();
        MedicalQueryServiceImpl service = new MedicalQueryServiceImpl(
                companyMapper, priceMapper, queryLogMapper, dataSource);

        MedicalQueryRequest request = new MedicalQueryRequest();
        request.setCompanyId(1L);
        request.setQueryType("medical_all");
        request.setRequestIp("127.0.0.1");
        request.setQueryParams(params("idCard", "430102199001011234"));

        MedicalQueryResult result = service.query(request);

        assertEquals(new BigDecimal("50.00"), result.getFee());
        assertEquals(new BigDecimal("100.00"), result.getBalanceAfter());
        assertEquals("A***e", result.getData().get("patientName"));
        assertEquals("430***********1234", result.getData().get("idCard"));
        assertEquals("Hy***", result.getData().get("diagnosis"));
        assertEquals(new BigDecimal("100.00"), companyMapper.company.getBalance());

        assertEquals(1, queryLogMapper.logs.size());
        BizQueryLog log = queryLogMapper.logs.get(0);
        assertEquals(Long.valueOf(1L), log.getId());
        assertEquals(Long.valueOf(1L), log.getCompanyId());
        assertEquals("medical_all", log.getQueryType());
        assertEquals(new BigDecimal("50.00"), log.getFee());
        assertEquals("0", log.getStatus());
        assertTrue(log.getQueryParams().contains("430102199001011234"));

        assertTrue(feeFlowMapper.flows.isEmpty());
        assertEquals(1, dataSource.queryCount);
    }

    private static MedicalQueryRequest request(Long companyId, String queryType)
    {
        MedicalQueryRequest request = new MedicalQueryRequest();
        request.setCompanyId(companyId);
        request.setQueryType(queryType);
        request.setRequestIp("127.0.0.1");
        request.setQueryParams(params("idCard", "430102199001011234"));
        return request;
    }

    @Test
    public void queryReturns4001WhenBalanceIsLowerThanFee()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "20.00"));
        FakePriceMapper priceMapper = new FakePriceMapper(price("medical_all", "50.00"));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        FakeFeeFlowMapper feeFlowMapper = new FakeFeeFlowMapper();
        FakeMedicalDataSource dataSource = new FakeMedicalDataSource();
        MedicalQueryServiceImpl service = new MedicalQueryServiceImpl(
                companyMapper, priceMapper, queryLogMapper, dataSource);

        MedicalQueryRequest request = new MedicalQueryRequest();
        request.setCompanyId(1L);
        request.setQueryType("medical_all");
        request.setQueryParams(params("idCard", "430102199001011234"));

        try
        {
            service.query(request);
            fail("Expected balance error");
        }
        catch (MedicalQueryException ex)
        {
            assertEquals("4001", ex.getCode());
        }

        assertEquals(new BigDecimal("20.00"), companyMapper.company.getBalance());
        assertTrue(queryLogMapper.logs.isEmpty());
        assertTrue(feeFlowMapper.flows.isEmpty());
        assertEquals(0, dataSource.queryCount);
    }

    @Test
    public void queryReturns4001WhenBalanceIsNegativeAfterSettlement()
    {
        FakeCompanyMapper companyMapper = new FakeCompanyMapper(company(1L, "-1.00"));
        FakePriceMapper priceMapper = new FakePriceMapper(price("medical_all", "50.00"));
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        FakeFeeFlowMapper feeFlowMapper = new FakeFeeFlowMapper();
        FakeMedicalDataSource dataSource = new FakeMedicalDataSource();
        MedicalQueryServiceImpl service = new MedicalQueryServiceImpl(
                companyMapper, priceMapper, queryLogMapper, dataSource);

        MedicalQueryRequest request = new MedicalQueryRequest();
        request.setCompanyId(1L);
        request.setQueryType("medical_all");
        request.setQueryParams(params("idCard", "430102199001011234"));

        try
        {
            service.query(request);
            fail("Expected balance error");
        }
        catch (MedicalQueryException ex)
        {
            assertEquals("4001", ex.getCode());
        }

        assertTrue(queryLogMapper.logs.isEmpty());
        assertTrue(feeFlowMapper.flows.isEmpty());
        assertEquals(0, dataSource.queryCount);
    }

    private static BizInsuranceCompany company(Long id, String balance)
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(id);
        company.setBalance(new BigDecimal(balance));
        company.setStatus("0");
        return company;
    }

    private static BizQueryPrice price(String queryType, String fee)
    {
        BizQueryPrice price = new BizQueryPrice();
        price.setQueryType(queryType);
        price.setFee(new BigDecimal(fee));
        price.setStatus("0");
        return price;
    }

    private static Map<String, Object> params(String key, String value)
    {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    private static class FakeCompanyMapper implements BizInsuranceCompanyMapper
    {
        private final BizInsuranceCompany company;

        FakeCompanyMapper(BizInsuranceCompany company)
        {
            this.company = company;
        }

        @Override
        public BizInsuranceCompany selectBizInsuranceCompanyById(Long id)
        {
            return company;
        }

        @Override
        public int deductBalance(Long companyId, BigDecimal amount)
        {
            if (company.getBalance().compareTo(amount) < 0)
            {
                return 0;
            }
            company.setBalance(company.getBalance().subtract(amount));
            return 1;
        }

        @Override public BizInsuranceCompany selectBizInsuranceCompanyByAppKey(String appKey) { return null; }
        @Override public BizInsuranceCompany selectBizInsuranceCompanyByUsername(String username) { return null; }
        @Override public int updateBizInsuranceCompanyLoginInfo(BizInsuranceCompany company) { return 0; }
        @Override public int addBalance(Long companyId, BigDecimal amount) { return 0; }
        @Override public int settleBalance(Long companyId, BigDecimal amount, java.util.Date balanceUpdateTime) { return 0; }
        @Override public List<BizInsuranceCompany> selectBizInsuranceCompanyList(BizInsuranceCompany company) { return new ArrayList<>(); }
        @Override public int insertBizInsuranceCompany(BizInsuranceCompany company) { return 0; }
        @Override public int updateBizInsuranceCompany(BizInsuranceCompany company) { return 0; }
        @Override public int deleteBizInsuranceCompanyByIds(Long[] ids) { return 0; }
    }

    private static class FakePriceMapper implements BizQueryPriceMapper
    {
        private final BizQueryPrice price;

        FakePriceMapper(BizQueryPrice price)
        {
            this.price = price;
        }

        @Override public BizQueryPrice selectBizQueryPriceByQueryType(String queryType) { return price; }
        @Override public BizQueryPrice selectBizQueryPriceById(Long id) { return null; }
        @Override public List<BizQueryPrice> selectBizQueryPriceList(BizQueryPrice price) { return new ArrayList<>(); }
        @Override public int insertBizQueryPrice(BizQueryPrice price) { return 0; }
        @Override public int updateBizQueryPrice(BizQueryPrice price) { return 0; }
        @Override public int deleteBizQueryPriceByIds(Long[] ids) { return 0; }
    }

    private static class FakeQueryLogMapper implements BizQueryLogMapper
    {
        private final List<BizQueryLog> logs = new ArrayList<>();

        @Override
        public int insertBizQueryLog(BizQueryLog queryLog)
        {
            queryLog.setId((long) logs.size() + 1);
            logs.add(queryLog);
            return 1;
        }

        @Override public BizQueryLog selectBizQueryLogById(Long id) { return null; }
        @Override public List<BizQueryLog> selectBizQueryLogList(BizQueryLog queryLog) { return logs; }
        @Override public BigDecimal sumUnsettledSuccessFeeByCompanyId(Long companyId, java.util.Date cutoffTime) { return BigDecimal.ZERO; }
        @Override public int updateSettlementIdForUnsettledSuccessLogs(Long companyId, Long settlementId, java.util.Date cutoffTime) { return 0; }
    }

    private static class FakeCompanyQueryPriceMapper implements com.ruoyi.business.mapper.BizCompanyQueryPriceMapper
    {
        private final com.ruoyi.business.domain.BizCompanyQueryPrice price;

        FakeCompanyQueryPriceMapper(Long companyId, String queryType, String hitFee, String noResultFee)
        {
            price = new com.ruoyi.business.domain.BizCompanyQueryPrice();
            price.setId(1L);
            price.setCompanyId(companyId);
            price.setQueryType(queryType);
            price.setQueryName(queryType);
            price.setHitFee(new BigDecimal(hitFee));
            price.setNoResultFee(new BigDecimal(noResultFee));
            price.setStatus("0");
        }

        @Override public com.ruoyi.business.domain.BizCompanyQueryPrice selectActivePrice(Long companyId, String queryType) { return price; }
        @Override public com.ruoyi.business.domain.BizCompanyQueryPrice selectCompanyPrice(Long companyId, String queryType) { return price; }
        @Override public com.ruoyi.business.domain.BizCompanyQueryPrice selectBizCompanyQueryPriceById(Long id) { return null; }
        @Override public List<com.ruoyi.business.domain.BizCompanyQueryPrice> selectBizCompanyQueryPriceList(com.ruoyi.business.domain.BizCompanyQueryPrice price) { return new ArrayList<>(); }
        @Override public int insertBizCompanyQueryPrice(com.ruoyi.business.domain.BizCompanyQueryPrice price) { return 0; }
        @Override public int updateBizCompanyQueryPrice(com.ruoyi.business.domain.BizCompanyQueryPrice price) { return 0; }
        @Override public int deleteBizCompanyQueryPriceByIds(Long[] ids) { return 0; }
    }

    private static class FakeMonthlyUsageMapper implements com.ruoyi.business.mapper.BizMonthlyUsageMapper
    {
        private BigDecimal usedAmount = BigDecimal.ZERO;
        private BigDecimal reservedAmount = BigDecimal.ZERO;

        @Override
        public int ensureUsage(Long companyId, String billingMonth, BigDecimal budgetAmount)
        {
            return 1;
        }

        @Override
        public int reserveBudget(Long companyId, String billingMonth, BigDecimal budgetAmount, BigDecimal reserveAmount)
        {
            if (usedAmount.add(reservedAmount).add(reserveAmount).compareTo(budgetAmount) > 0)
            {
                return 0;
            }
            reservedAmount = reservedAmount.add(reserveAmount);
            return 1;
        }

        @Override
        public int confirmBudget(Long companyId, String billingMonth, BigDecimal reserveAmount, BigDecimal actualAmount)
        {
            reservedAmount = reservedAmount.subtract(reserveAmount);
            usedAmount = usedAmount.add(actualAmount);
            return 1;
        }

        @Override
        public int releaseBudget(Long companyId, String billingMonth, BigDecimal reserveAmount)
        {
            reservedAmount = reservedAmount.subtract(reserveAmount);
            return 1;
        }

        @Override public com.ruoyi.business.domain.BizMonthlyUsage selectUsage(Long companyId, String billingMonth) { return null; }
        @Override public List<com.ruoyi.business.domain.BizMonthlyUsage> selectBizMonthlyUsageList(com.ruoyi.business.domain.BizMonthlyUsage usage) { return new ArrayList<>(); }
    }

    private static class FakeFeeFlowMapper implements BizFeeFlowMapper
    {
        private final List<BizFeeFlow> flows = new ArrayList<>();

        @Override public int insertBizFeeFlow(BizFeeFlow flow) { flows.add(flow); return 1; }
        @Override public BizFeeFlow selectBizFeeFlowById(Long id) { return null; }
        @Override public List<BizFeeFlow> selectBizFeeFlowList(BizFeeFlow flow) { return flows; }
    }

    private static class FakeMedicalDataSource implements MedicalDataSource
    {
        private int queryCount;

        @Override
        public Map<String, Object> query(MedicalQueryRequest request)
        {
            queryCount++;
            if (emptyResult)
            {
                return new HashMap<>();
            }
            Map<String, Object> data = new HashMap<>();
            data.put("patientName", "Alice");
            data.put("idCard", "430102199001011234");
            data.put("diagnosis", "Hypertension");
            return data;
        }

        @Override public boolean health() { return true; }

        private boolean emptyResult;
    }
}
