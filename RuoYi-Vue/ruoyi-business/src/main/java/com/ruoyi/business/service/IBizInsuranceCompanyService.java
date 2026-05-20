package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.BizInsuranceCompany;

/**
 * 保险公司 服务层
 */
public interface IBizInsuranceCompanyService
{
    public BizInsuranceCompany selectBizInsuranceCompanyById(Long id);

    public BizInsuranceCompany selectBizInsuranceCompanyByAppKey(String appKey);

    public List<BizInsuranceCompany> selectBizInsuranceCompanyList(BizInsuranceCompany company);

    public int insertBizInsuranceCompany(BizInsuranceCompany company);

    public int updateBizInsuranceCompany(BizInsuranceCompany company);

    public int deleteBizInsuranceCompanyByIds(Long[] ids);

    /**
     * 充值
     */
    /**
     * 根据用户名查询（用于登录）
     */
    public BizInsuranceCompany selectBizInsuranceCompanyByUsername(String username);

    /**
     * 更新登录信息（IP和时间）
     */
    public int updateLoginInfo(BizInsuranceCompany company);

    public int deductBalance(Long companyId, BigDecimal amount);

    /**
     * 启用/停用
     */
    public int changeStatus(Long id, String status);

    public int updatePassword(Long id, String password);

    public String regenerateAppKey(Long id);
}
