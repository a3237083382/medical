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
 * 保险公司对象 biz_insurance_company
 */
public class BizInsuranceCompany extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 公司名称 */
    @NotBlank(message = "公司名称不能为空")
    @Excel(name = "公司名称")
    private String companyName;

    /** 公司编码 */
    @Excel(name = "公司编码")
    private String companyCode;

    /** 登录用户名 */
    @Excel(name = "登录用户名")
    private String username;

    /** 登录密码（bcrypt加密） */
    private String password;

    /** 最后登录IP */
    @Excel(name = "最后登录IP")
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后登录时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate;

    /** AppKey */
    @Excel(name = "AppKey")
    private String appKey;

    /** AppSecret */
    private String appSecret;

    /** 账户余额 */
    @Excel(name = "账户余额", cellType = ColumnType.NUMERIC)
    private BigDecimal balance;

    /** 结算周期（天） */
    @Excel(name = "结算周期(天)", cellType = ColumnType.NUMERIC)
    private Integer billingCycleDays;

    /** 余额最后更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "余额更新时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date balanceUpdateTime;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
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

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Integer getBillingCycleDays() { return billingCycleDays; }
    public void setBillingCycleDays(Integer billingCycleDays) { this.billingCycleDays = billingCycleDays; }

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
            .append("balanceUpdateTime", getBalanceUpdateTime())
            .append("status", getStatus())
            .append("contactPerson", getContactPerson())
            .append("contactPhone", getContactPhone())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}
