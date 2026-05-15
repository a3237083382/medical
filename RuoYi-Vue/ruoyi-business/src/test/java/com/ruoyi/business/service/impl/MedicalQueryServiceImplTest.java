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

    @Test
    public void queryAllowsWhenBalanceIsNonNegativeEvenIfFeeIsHigher()
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

        MedicalQueryResult result = service.query(request);

        assertEquals(new BigDecimal("20.00"), companyMapper.company.getBalance());
        assertEquals(new BigDecimal("20.00"), result.getBalanceAfter());
        assertEquals(1, queryLogMapper.logs.size());
        assertTrue(feeFlowMapper.flows.isEmpty());
        assertEquals(1, dataSource.queryCount);
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
            Map<String, Object> data = new HashMap<>();
            data.put("patientName", "Alice");
            data.put("idCard", "430102199001011234");
            data.put("diagnosis", "Hypertension");
            return data;
        }

        @Override public boolean health() { return true; }
    }
}
