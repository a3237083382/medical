package com.ruoyi.quartz.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ScheduleUtilsTest
{
    @Test
    public void whiteListAllowsBusinessSettlementTaskPackage()
    {
        assertTrue(ScheduleUtils.whiteList("com.ruoyi.business.task.BillingSettlementTask.settleDueCompanies(7)"));
    }
}
