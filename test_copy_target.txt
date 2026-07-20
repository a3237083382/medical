package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 淇濋櫓鍏徃瀵硅薄 biz_insurance_company
 */
public class BizInsuranceCompany extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 涓婚敭ID */
    private Long id;

    /** 鍏徃鍚嶇О */
    @NotBlank(message = "鍏徃鍚嶇О涓嶈兘涓虹┖")
    @Excel(name = "鍏徃鍚嶇О")
    private String companyName;

    /** 鍏徃缂栫爜 */
    @Excel(name = "鍏徃缂栫爜")
    private String companyCode;

    /** 鐧诲綍鐢ㄦ埛鍚?*/
    @Excel(name = "鐧诲綍鐢ㄦ埛鍚?)
    private String username;

    /** 鐧诲綍瀵嗙爜锛坆crypt鍔犲瘑锛?*/
    private String password;

    /** 鏈€鍚庣櫥褰旾P */
    @Excel(name = "鏈€鍚庣櫥褰旾P")
    private String loginIp;

    /** 鏈€鍚庣櫥褰曟椂闂?*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "鏈€鍚庣櫥褰曟椂闂?, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate;

    /** AppKey */
    @Excel(name = "AppKey")
    private String appKey;


    /** 璐︽埛浣欓 */
    @Excel(name = "璐︽埛浣欓", cellType = ColumnType.NUMERIC)
    private BigDecimal balance;

    /** 缁撶畻鍛ㄦ湡锛堝ぉ锛?*/
    @Excel(name = "缁撶畻鍛ㄦ湡(澶?", cellType = ColumnType.NUMERIC)
    private Integer billingCycleDays;

    @Excel(name = "鏈堝害棰勭畻", cellType = ColumnType.NUMERIC)
    private BigDecimal monthlyBudget;

    @Excel(name = "棰勭畻鐘舵€?, readConverterExp = "0=鍚敤,1=鍋滅敤")
    private String budgetEnabled;

    /** 浣欓鏈€鍚庢洿鏂版椂闂?*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "浣欓鏇存柊鏃堕棿", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date balanceUpdateTime;

    /** 鐘舵€侊紙0姝ｅ父 1鍋滅敤锛?*/
    @Excel(name = "鐘舵€?, readConverterExp = "0=姝ｅ父,1=鍋滅敤")
    private String status;

    /** 鑱旂郴浜?*/
    @Excel(name = "鑱旂郴浜?)
    private String contactPerson;

    /** 鑱旂郴鐢佃瘽 */
    @Excel(name = "鑱旂郴鐢佃瘽")
    private String contactPhone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getLoginIp() { return loginIp; }
    public void setLoginIp(String loginIp) { this.loginIp = loginIp; }

    public Date getLoginDate() { return loginDate; }
    public void setLoginDate(Date loginDate) { this.loginDate = loginDate; }

    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Integer getBillingCycleDays() { return billingCycleDays; }
    public void setBillingCycleDays(Integer billingCycleDays) { this.billingCycleDays = billingCycleDays; }

    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public String getBudgetEnabled() { return budgetEnabled; }
    public void setBudgetEnabled(String budgetEnabled) { this.budgetEnabled = budgetEnabled; }

    public Date getBalanceUpdateTime() { return balanceUpdateTime; }
    public void setBalanceUpdateTime(Date balanceUpdateTime) { this.balanceUpdateTime = balanceUpdateTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("companyName", getCompanyName())
            .append("companyCode", getCompanyCode())
            .append("appKey", getAppKey())
            .append("balance", getBalance())
            .append("billingCycleDays", getBillingCycleDays())
            .append("monthlyBudget", getMonthlyBudget())
            .append("budgetEnabled", getBudgetEnabled())
            .append("balanceUpdateTime", getBalanceUpdateTime())
            .append("status", getStatus())
            .append("contactPerson", getContactPerson())
            .append("contactPhone", getContactPhone())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}
