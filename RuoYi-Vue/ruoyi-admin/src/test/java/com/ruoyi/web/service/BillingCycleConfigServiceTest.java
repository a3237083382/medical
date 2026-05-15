package com.ruoyi.web.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;

import com.ruoyi.common.exception.job.TaskException;
import com.ruoyi.quartz.domain.SysJob;
import com.ruoyi.quartz.service.ISysJobService;

public class BillingCycleConfigServiceTest
{
    @Test
    public void getBillingCycleDaysReadsCycleFromScheduleInvokeTarget()
    {
        FakeJobService jobService = new FakeJobService("billingSettlementTask.settleDueCompanies(14)");
        BillingCycleConfigService service = new BillingCycleConfigService(jobService);

        assertEquals(14, service.getBillingCycleDays());
    }

    @Test
    public void getBillingCycleDaysDefaultsToThirtyWhenScheduleHasNoParam()
    {
        FakeJobService jobService = new FakeJobService("billingSettlementTask.settleDueCompanies()");
        BillingCycleConfigService service = new BillingCycleConfigService(jobService);

        assertEquals(30, service.getBillingCycleDays());
    }

    private static class FakeJobService implements ISysJobService
    {
        private final String invokeTarget;

        FakeJobService(String invokeTarget)
        {
            this.invokeTarget = invokeTarget;
        }

        @Override
        public List<SysJob> selectJobList(SysJob job)
        {
            List<SysJob> jobs = new ArrayList<>();
            SysJob result = new SysJob();
            result.setInvokeTarget(invokeTarget);
            jobs.add(result);
            return jobs;
        }

        @Override public SysJob selectJobById(Long jobId) { return null; }
        @Override public int pauseJob(SysJob job) throws SchedulerException { return 0; }
        @Override public int resumeJob(SysJob job) throws SchedulerException { return 0; }
        @Override public int deleteJob(SysJob job) throws SchedulerException { return 0; }
        @Override public void deleteJobByIds(Long[] jobIds) throws SchedulerException { }
        @Override public int changeStatus(SysJob job) throws SchedulerException { return 0; }
        @Override public boolean run(SysJob job) throws SchedulerException { return false; }
        @Override public int insertJob(SysJob job) throws SchedulerException, TaskException { return 0; }
        @Override public int updateJob(SysJob job) throws SchedulerException, TaskException { return 0; }
        @Override public boolean checkCronExpressionIsValid(String cronExpression) { return false; }
    }
}
