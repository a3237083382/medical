package com.ruoyi.business.domain.dashboard;

import java.math.BigDecimal;

public class BusinessStatItem
{
    private String name;
    private Long value;
    private BigDecimal amount;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

