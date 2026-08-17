package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizDelayedQueryRequest;
import com.ruoyi.business.domain.BizDelayedQueryResult;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizMonthlyUsage;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizDelayedQueryRequestMapper;
import com.ruoyi.business.mapper.BizDelayedQueryResultMapper;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.mapper.BizQueryLogMapper;

public class BizDelayedQueryServiceImplTest
{
    @Test
    public void submitReservesHighestMonthlyBudgetBeforeProcessing()
    {
        FakeRequestMapper requestMapper = new FakeRequestMapper(null);
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        FakeMonthlyUsageMapper monthlyUsageMapper = new FakeMonthlyUsageMapper();
        BizDelayedQueryServiceImpl service = service(requestMapper, new FakeResultMapper(), queryLogMapper, monthlyUsageMapper);

        BizDelayedQueryRequest submitted = service.submit(10L, "测试保险公司", "李四", "430102199202021234", "127.0.0.1");

        assertEquals("PENDING", submitted.getQueryStatus());
        assertEquals("NOT_UPLOADED", submitted.getUploadStatus());
        assertEquals(new BigDecimal("20.00"), submitted.getReservedFee());
        assertEquals(new BigDecimal("20.00"), monthlyUsageMapper.reservedAmount);
        assertTrue(queryLogMapper.logs.isEmpty());
    }

    @Test
    public void submitRejectsMissingPatientIdentity()
    {
        BizDelayedQueryServiceImpl service = service(new FakeRequestMapper(null), new FakeResultMapper(),
                new FakeQueryLogMapper(), new FakeMonthlyUsageMapper());

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(10L, "测试保险公司", "", "430102199202021234", "127.0.0.1"));
        assertThrows(IllegalArgumentException.class,
                () -> service.submit(10L, "测试保险公司", "李四", "", "127.0.0.1"));
    }

    @Test
    public void submitBatchCreatesOnePendingRequestPerPerson()
    {
        FakeRequestMapper requestMapper = new FakeRequestMapper(null);
        FakeMonthlyUsageMapper monthlyUsageMapper = new FakeMonthlyUsageMapper();
        BizDelayedQueryServiceImpl service = service(requestMapper, new FakeResultMapper(),
                new FakeQueryLogMapper(), monthlyUsageMapper);

        List<BizDelayedQueryRequest> submitted = service.submitBatch(10L, "测试保险公司",
                List.of(batchItem("张三", "430102199001011234"), batchItem("李四", "430102199202021234")),
                "127.0.0.1");

        assertEquals(2, submitted.size());
        assertEquals(2, requestMapper.insertedRequests.size());
        assertEquals("PENDING", submitted.get(0).getQueryStatus());
        assertEquals("NOT_UPLOADED", submitted.get(1).getUploadStatus());
        assertEquals(new BigDecimal("40.00"), monthlyUsageMapper.reservedAmount);
    }

    @Test
    public void submitBatchReusesUnfinishedDuplicateRequest()
    {
        BizDelayedQueryRequest existing = pendingRequest();
        FakeRequestMapper requestMapper = new FakeRequestMapper(existing);
        FakeMonthlyUsageMapper monthlyUsageMapper = new FakeMonthlyUsageMapper();
        BizDelayedQueryServiceImpl service = service(requestMapper, new FakeResultMapper(),
                new FakeQueryLogMapper(), monthlyUsageMapper);

        List<BizDelayedQueryRequest> submitted = service.submitBatch(10L, "测试保险公司",
                List.of(batchItem(existing.getPatientName(), existing.getIdCard()),
                        batchItem("李四", "430102199202021234")),
                "127.0.0.1");

        assertEquals(2, submitted.size());
        assertEquals(existing.getRequestNo(), submitted.get(0).getRequestNo());
        assertEquals(1, requestMapper.insertedRequests.size());
        assertEquals(new BigDecimal("20.00"), monthlyUsageMapper.reservedAmount);
    }


    @Test
    public void saveDraftDoesNotExposeResultsOrChargeFee()
    {
        FakeRequestMapper requestMapper = new FakeRequestMapper(pendingRequest());
        FakeResultMapper resultMapper = new FakeResultMapper();
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        BizDelayedQueryServiceImpl service = service(requestMapper, resultMapper, queryLogMapper);

        BizDelayedQueryResult row = row("{\"hospital\":\"湘雅医院\"}");
        service.saveDraft(1L, List.of(row), "HIT", "已保存草稿", "operator");

        BizDelayedQueryRequest companyView = service.selectCompanyDetail(1L, 10L);

        assertEquals("NOT_UPLOADED", requestMapper.request.getUploadStatus());
        assertTrue(companyView.getResults().isEmpty());
        assertTrue(queryLogMapper.logs.isEmpty());
        assertNull(requestMapper.request.getFee());
    }

    @Test
    public void completeHitExposesResultsAndWritesBillingLog()
    {
        FakeRequestMapper requestMapper = new FakeRequestMapper(pendingRequest());
        FakeResultMapper resultMapper = new FakeResultMapper();
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        FakeMonthlyUsageMapper monthlyUsageMapper = new FakeMonthlyUsageMapper();
        BizDelayedQueryServiceImpl service = service(requestMapper, resultMapper, queryLogMapper, monthlyUsageMapper);

        service.complete(1L, List.of(row("{\"hospital\":\"湘雅医院\"}")), "HIT", "已查询到明细", "operator");
        BizDelayedQueryRequest companyView = service.selectCompanyDetail(1L, 10L);

        assertEquals("QUERIED", requestMapper.request.getQueryStatus());
        assertEquals("UPLOADED", requestMapper.request.getUploadStatus());
        assertEquals(1, companyView.getResults().size());
        assertEquals(new BigDecimal("20.00"), requestMapper.request.getFee());
        assertEquals(new BigDecimal("20.00"), monthlyUsageMapper.usedAmount);
        assertEquals(1, queryLogMapper.logs.size());
        assertEquals("delayed_precise", queryLogMapper.logs.get(0).getQueryType());
        assertEquals("HIT", queryLogMapper.logs.get(0).getResultStatus());
    }

    @Test
    public void completeNoResultDoesNotCharge()
    {
        BizDelayedQueryRequest request = pendingRequest();
        request.setReservedFee(new BigDecimal("20.00"));
        FakeRequestMapper requestMapper = new FakeRequestMapper(request);
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        FakeMonthlyUsageMapper monthlyUsageMapper = new FakeMonthlyUsageMapper();
        monthlyUsageMapper.reservedAmount = new BigDecimal("20.00");
        BizDelayedQueryServiceImpl service = service(requestMapper, new FakeResultMapper(), queryLogMapper, monthlyUsageMapper);

        service.complete(1L, new ArrayList<>(), "NO_RESULT", "未查询到该人员相关信息", "operator");

        assertEquals("UPLOADED", requestMapper.request.getUploadStatus());
        assertEquals(new BigDecimal("0.00"), requestMapper.request.getFee());
        assertEquals(new BigDecimal("0.00"), monthlyUsageMapper.usedAmount);
        assertEquals(new BigDecimal("0.00"), monthlyUsageMapper.reservedAmount);
        assertEquals(new BigDecimal("20.00"), monthlyUsageMapper.confirmedReserveAmount);
        assertEquals("NO_RESULT", queryLogMapper.logs.get(0).getResultStatus());
    }

    @Test
    public void completeHitRequiresAtLeastOneDetailRow()
    {
        FakeRequestMapper requestMapper = new FakeRequestMapper(pendingRequest());
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        BizDelayedQueryServiceImpl service = service(requestMapper, new FakeResultMapper(), queryLogMapper);

        assertThrows(IllegalArgumentException.class,
                () -> service.complete(1L, new ArrayList<>(), "HIT", "已查询到明细", "operator"));

        assertEquals("NOT_UPLOADED", requestMapper.request.getUploadStatus());
        assertTrue(queryLogMapper.logs.isEmpty());
    }

    @Test
    public void completePartialRequiresResultMessage()
    {
        FakeRequestMapper requestMapper = new FakeRequestMapper(pendingRequest());
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        BizDelayedQueryServiceImpl service = service(requestMapper, new FakeResultMapper(), queryLogMapper);

        assertThrows(IllegalArgumentException.class,
                () -> service.complete(1L, new ArrayList<>(), "PARTIAL", "", "operator"));

        assertEquals("NOT_UPLOADED", requestMapper.request.getUploadStatus());
        assertTrue(queryLogMapper.logs.isEmpty());
    }

    @Test
    public void updateUploadedResultKeepsVisibleLatestResultAndRecordsModifier()
    {
        FakeRequestMapper requestMapper = new FakeRequestMapper(completedRequest());
        FakeResultMapper resultMapper = new FakeResultMapper();
        resultMapper.replaceResults(1L, List.of(row("{\"hospital\":\"old\"}")));
        BizDelayedQueryServiceImpl service = service(requestMapper, resultMapper, new FakeQueryLogMapper());

        service.updateUploadedResult(1L, List.of(row("{\"hospital\":\"new\"}")), "HIT", "修正后结果", "auditor", "修正医院名称");
        BizDelayedQueryRequest companyView = service.selectCompanyDetail(1L, 10L);

        assertEquals(1, companyView.getResults().size());
        assertTrue(companyView.getResults().get(0).getRawJson().contains("new"));
        assertEquals("auditor", requestMapper.request.getModifyBy());
        assertEquals("修正医院名称", requestMapper.request.getModifyReason());
    }

    @Test
    public void selectCompanyLogsReturnsRealtimeAndDelayedLogsForSameCompany()
    {
        FakeRequestMapper requestMapper = new FakeRequestMapper(completedRequest());
        FakeQueryLogMapper queryLogMapper = new FakeQueryLogMapper();
        BizQueryLog realtimeLog = new BizQueryLog();
        realtimeLog.setCompanyId(10L);
        realtimeLog.setQueryType("medical_all");
        realtimeLog.setResultStatus("HIT");
        queryLogMapper.logs.add(realtimeLog);
        BizDelayedQueryServiceImpl service = service(requestMapper, new FakeResultMapper(), queryLogMapper);

        BizDelayedQueryServiceImpl.CompanyLogs logs = service.selectCompanyLogs(10L);

        assertEquals(1, logs.getRealtimeLogs().size());
        assertEquals(1, logs.getDelayedLogs().size());
        assertEquals("UPLOADED", logs.getDelayedLogs().get(0).getUploadStatus());
    }

    private static BizDelayedQueryServiceImpl service(FakeRequestMapper requestMapper,
            FakeResultMapper resultMapper, FakeQueryLogMapper queryLogMapper)
    {
        return service(requestMapper, resultMapper, queryLogMapper, new FakeMonthlyUsageMapper());
    }

    private static BizDelayedQueryServiceImpl service(FakeRequestMapper requestMapper,
            FakeResultMapper resultMapper, FakeQueryLogMapper queryLogMapper, FakeMonthlyUsageMapper monthlyUsageMapper)
    {
        return new BizDelayedQueryServiceImpl(requestMapper, resultMapper, new FakeCompanyMapper(),
                new FakeCompanyPriceMapper(), monthlyUsageMapper, queryLogMapper);
    }

    private static BizDelayedQueryRequest pendingRequest()
    {
        BizDelayedQueryRequest request = new BizDelayedQueryRequest();
        request.setId(1L);
        request.setRequestNo("DQ202608140001");
        request.setCompanyId(10L);
        request.setCompanyNameSnapshot("测试保险公司");
        request.setPatientName("张三");
        request.setIdCard("430102199001011234");
        request.setQueryStatus("PENDING");
        request.setUploadStatus("NOT_UPLOADED");
        request.setSubmitTime(new Date());
        return request;
    }

    private static BizDelayedQueryRequest completedRequest()
    {
        BizDelayedQueryRequest request = pendingRequest();
        request.setQueryStatus("QUERIED");
        request.setUploadStatus("UPLOADED");
        request.setResultStatus("HIT");
        return request;
    }

    private static BizDelayedQueryRequest batchItem(String patientName, String idCard)
    {
        BizDelayedQueryRequest request = new BizDelayedQueryRequest();
        request.setPatientName(patientName);
        request.setIdCard(idCard);
        return request;
    }

    private static BizDelayedQueryResult row(String rawJson)
    {
        BizDelayedQueryResult result = new BizDelayedQueryResult();
        result.setRawJson(rawJson);
        return result;
    }

    private static class FakeRequestMapper implements BizDelayedQueryRequestMapper
    {
        private BizDelayedQueryRequest request;
        private final List<BizDelayedQueryRequest> insertedRequests = new ArrayList<>();

        FakeRequestMapper(BizDelayedQueryRequest request)
        {
            this.request = request;
        }

        @Override public BizDelayedQueryRequest selectBizDelayedQueryRequestById(Long id) { return request; }
        @Override public BizDelayedQueryRequest selectPendingDuplicate(Long companyId, String patientName, String idCard)
        {
            if (matchesPending(request, companyId, patientName, idCard))
            {
                return request;
            }
            for (BizDelayedQueryRequest item : insertedRequests)
            {
                if (matchesPending(item, companyId, patientName, idCard))
                {
                    return item;
                }
            }
            return null;
        }
        @Override public List<BizDelayedQueryRequest> selectBizDelayedQueryRequestList(BizDelayedQueryRequest filter) { return List.of(request); }
        @Override public int insertBizDelayedQueryRequest(BizDelayedQueryRequest request)
        {
            request.setId((long) insertedRequests.size() + 1);
            insertedRequests.add(request);
            this.request = request;
            return 1;
        }
        @Override public int updateBizDelayedQueryRequest(BizDelayedQueryRequest request) { copy(request, this.request); return 1; }

        private boolean matchesPending(BizDelayedQueryRequest item, Long companyId, String patientName, String idCard)
        {
            return item != null
                    && companyId.equals(item.getCompanyId())
                    && patientName.equals(item.getPatientName())
                    && idCard.equals(item.getIdCard())
                    && ("PENDING".equals(item.getQueryStatus()) || "NOT_UPLOADED".equals(item.getUploadStatus()));
        }

        private void copy(BizDelayedQueryRequest source, BizDelayedQueryRequest target)
        {
            if (source.getQueryStatus() != null) target.setQueryStatus(source.getQueryStatus());
            if (source.getUploadStatus() != null) target.setUploadStatus(source.getUploadStatus());
            if (source.getResultStatus() != null) target.setResultStatus(source.getResultStatus());
            if (source.getResultMessage() != null) target.setResultMessage(source.getResultMessage());
            if (source.getHandlerName() != null) target.setHandlerName(source.getHandlerName());
            if (source.getHandledTime() != null) target.setHandledTime(source.getHandledTime());
            if (source.getUploadedTime() != null) target.setUploadedTime(source.getUploadedTime());
            if (source.getFee() != null) target.setFee(source.getFee());
            if (source.getReservedFee() != null) target.setReservedFee(source.getReservedFee());
            if (source.getBillingMonth() != null) target.setBillingMonth(source.getBillingMonth());
            if (source.getChargedFlag() != null) target.setChargedFlag(source.getChargedFlag());
            if (source.getPriceConfigId() != null) target.setPriceConfigId(source.getPriceConfigId());
            if (source.getModifyBy() != null) target.setModifyBy(source.getModifyBy());
            if (source.getModifyTime() != null) target.setModifyTime(source.getModifyTime());
            if (source.getModifyReason() != null) target.setModifyReason(source.getModifyReason());
        }
    }

    private static class FakeResultMapper implements BizDelayedQueryResultMapper
    {
        private final List<BizDelayedQueryResult> rows = new ArrayList<>();

        @Override public List<BizDelayedQueryResult> selectByRequestId(Long requestId) { return rows; }
        @Override public int deleteByRequestId(Long requestId) { rows.clear(); return 1; }
        @Override public int insertBizDelayedQueryResult(BizDelayedQueryResult result) { rows.add(result); return 1; }

        void replaceResults(Long requestId, List<BizDelayedQueryResult> results)
        {
            rows.clear();
            rows.addAll(results);
        }
    }

    private static class FakeCompanyMapper implements BizInsuranceCompanyMapper
    {
        @Override public BizInsuranceCompany selectBizInsuranceCompanyById(Long id)
        {
            BizInsuranceCompany company = new BizInsuranceCompany();
            company.setId(id);
            company.setCompanyName("测试保险公司");
            company.setMonthlyBudget(new BigDecimal("100.00"));
            company.setBudgetEnabled("0");
            company.setStatus("0");
            return company;
        }

        @Override public BizInsuranceCompany selectBizInsuranceCompanyByIdForUpdate(Long id)
        {
            return selectBizInsuranceCompanyById(id);
        }

        @Override public BizInsuranceCompany selectBizInsuranceCompanyByAppKey(String appKey) { return null; }
        @Override public BizInsuranceCompany selectBizInsuranceCompanyByUsername(String username) { return null; }
        @Override public int updateBizInsuranceCompanyLoginInfo(BizInsuranceCompany company) { return 0; }
        @Override public int deductBalance(Long companyId, BigDecimal amount) { return 0; }
        @Override public int addBalance(Long companyId, BigDecimal amount) { return 0; }
        @Override public int settleBalance(Long companyId, BigDecimal amount, Date balanceUpdateTime) { return 0; }
        @Override public List<BizInsuranceCompany> selectBizInsuranceCompanyList(BizInsuranceCompany company) { return new ArrayList<>(); }
        @Override public int insertBizInsuranceCompany(BizInsuranceCompany company) { return 0; }
        @Override public int updateBizInsuranceCompany(BizInsuranceCompany company) { return 0; }
        @Override public int deleteBizInsuranceCompanyByIds(Long[] ids) { return 0; }
    }

    private static class FakeCompanyPriceMapper implements BizCompanyQueryPriceMapper
    {
        @Override public BizCompanyQueryPrice selectActivePrice(Long companyId, String queryType)
        {
            BizCompanyQueryPrice price = new BizCompanyQueryPrice();
            price.setId(9L);
            price.setCompanyId(companyId);
            price.setQueryType(queryType);
            price.setHitFee(new BigDecimal("20.00"));
            price.setNoResultFee(new BigDecimal("5.00"));
            price.setStatus("0");
            return price;
        }

        @Override public BizCompanyQueryPrice selectCompanyPrice(Long companyId, String queryType) { return selectActivePrice(companyId, queryType); }
        @Override public BizCompanyQueryPrice selectBizCompanyQueryPriceById(Long id) { return null; }
        @Override public List<BizCompanyQueryPrice> selectBizCompanyQueryPriceList(BizCompanyQueryPrice price) { return new ArrayList<>(); }
        @Override public int insertBizCompanyQueryPrice(BizCompanyQueryPrice price) { return 0; }
        @Override public int updateBizCompanyQueryPrice(BizCompanyQueryPrice price) { return 0; }
        @Override public int deleteBizCompanyQueryPriceByIds(Long[] ids) { return 0; }
    }

    private static class FakeMonthlyUsageMapper implements BizMonthlyUsageMapper
    {
        private BigDecimal usedAmount = BigDecimal.ZERO;
        private BigDecimal reservedAmount = BigDecimal.ZERO;
        private BigDecimal confirmedReserveAmount = BigDecimal.ZERO;

        @Override public int ensureUsage(Long companyId, String billingMonth, BigDecimal budgetAmount) { return 1; }
        @Override public int reserveBudget(Long companyId, String billingMonth, BigDecimal budgetAmount, BigDecimal reserveAmount) { this.reservedAmount = this.reservedAmount.add(reserveAmount); return 1; }
        @Override public int confirmBudget(Long companyId, String billingMonth, BigDecimal reserveAmount, BigDecimal actualAmount) { confirmedReserveAmount = reserveAmount; reservedAmount = reservedAmount.subtract(reserveAmount); usedAmount = usedAmount.add(actualAmount); return 1; }
        @Override public int releaseBudget(Long companyId, String billingMonth, BigDecimal reserveAmount) { reservedAmount = reservedAmount.subtract(reserveAmount); return 1; }
        @Override public BizMonthlyUsage selectUsage(Long companyId, String billingMonth) { return null; }
        @Override public List<BizMonthlyUsage> selectBizMonthlyUsageList(BizMonthlyUsage usage) { return new ArrayList<>(); }
    }

    private static class FakeQueryLogMapper implements BizQueryLogMapper
    {
        private final List<BizQueryLog> logs = new ArrayList<>();

        @Override public int insertBizQueryLog(BizQueryLog queryLog) { logs.add(queryLog); return 1; }
        @Override public BizQueryLog selectBizQueryLogById(Long id) { return null; }
        @Override public List<BizQueryLog> selectBizQueryLogList(BizQueryLog queryLog)
        {
            List<BizQueryLog> filtered = new ArrayList<>();
            for (BizQueryLog log : logs)
            {
                if (queryLog.getCompanyId() == null || queryLog.getCompanyId().equals(log.getCompanyId()))
                {
                    filtered.add(log);
                }
            }
            return filtered;
        }
        @Override public BigDecimal sumUnsettledSuccessFeeByCompanyId(Long companyId, Date cutoffTime) { return BigDecimal.ZERO; }
        @Override public int updateSettlementIdForUnsettledSuccessLogs(Long companyId, Long settlementId, Date cutoffTime) { return 0; }
    }
}
