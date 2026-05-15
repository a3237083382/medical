package com.ruoyi.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.service.ISysJobService;

@Service
public class BillingCycleConfigService
{
    private static final int DEFAULT_BILLING_CYCLE_DAYS = 30;
    private static final String SETTLEMENT_TARGET = "billingSettlementTask.settleDueCompanies";

    private final ISysJobService jobService;

    public BillingCycleConfigService(ISysJobService jobService)
    {
        this.jobService = jobService;
    }

    public Integer getBillingCycleDays()
    {
        SysJob query = new SysJob();
        query.setInvokeTarget(SETTLEMENT_TARGET);
        query.setStatus("0");
        List<SysJob> jobs = jobService.selectJobList(query);
        for (SysJob job : jobs)
        {
            Integer cycleDays = parseCycleDays(job.getInvokeTarget());
            if (cycleDays != null)
            {
                return cycleDays;
            }
        }
        return DEFAULT_BILLING_CYCLE_DAYS;
    }

    private Integer parseCycleDays(String invokeTarget)
    {
        if (invokeTarget == null || !invokeTarget.startsWith(SETTLEMENT_TARGET + "("))
        {
            return null;
        }
        int start = invokeTarget.indexOf('(');
        int end = invokeTarget.indexOf(')', start + 1);
        if (end <= start)
        {
            return DEFAULT_BILLING_CYCLE_DAYS;
        }
        String value = invokeTarget.substring(start + 1, end).trim();
        if (value.isEmpty())
        {
            return DEFAULT_BILLING_CYCLE_DAYS;
        }
        try
        {
            int cycleDays = Integer.parseInt(value);
            return cycleDays > 0 ? cycleDays : DEFAULT_BILLING_CYCLE_DAYS;
        }
        catch (NumberFormatException e)
        {
            return DEFAULT_BILLING_CYCLE_DAYS;
        }
    }
}
