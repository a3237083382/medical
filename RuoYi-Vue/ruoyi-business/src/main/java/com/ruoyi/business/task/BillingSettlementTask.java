package com.ruoyi.business.task;

import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.business.service.impl.BillingSettlementService;

@Component("billingSettlementTask")
public class BillingSettlementTask
{
    private static final Logger log = LoggerFactory.getLogger(BillingSettlementTask.class);

    private final BillingSettlementService settlementService;

    public BillingSettlementTask(BillingSettlementService settlementService)
    {
        this.settlementService = settlementService;
    }

    public void settleDueCompanies()
    {
        int count = settlementService.settleDueCompanies();
        log.info("billing settlement finished, companyCount={}", count);
    }

    public void settleDueCompanies(Integer billingCycleDays)
    {
        int count = settlementService.settleDueCompanies(billingCycleDays);
        log.info("billing settlement finished, cycleDays={}, companyCount={}", billingCycleDays, count);
    }

    public void generateMonthlyBills()
    {
        String billingMonth = YearMonth.now().minusMonths(1).toString();
        generateMonthlyBills(billingMonth);
    }

    public void generateMonthlyBills(String billingMonth)
    {
        int count = settlementService.generateMonthlyBills(billingMonth);
        log.info("monthly bill generation finished, billingMonth={}, companyCount={}", billingMonth, count);
    }
}
