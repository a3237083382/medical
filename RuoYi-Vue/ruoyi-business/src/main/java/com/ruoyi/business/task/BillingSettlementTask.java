package com.ruoyi.business.task;

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
}
