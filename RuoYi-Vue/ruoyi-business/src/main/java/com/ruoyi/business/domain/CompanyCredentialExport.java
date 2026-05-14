package com.ruoyi.business.domain;

import com.ruoyi.common.annotation.Excel;

public class CompanyCredentialExport
{
    @Excel(name = "公司名称")
    private String companyName;

    @Excel(name = "公司编码")
    private String companyCode;

    @Excel(name = "AppKey")
    private String appKey;

    @Excel(name = "AppSecret")
    private String appSecret;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
}
