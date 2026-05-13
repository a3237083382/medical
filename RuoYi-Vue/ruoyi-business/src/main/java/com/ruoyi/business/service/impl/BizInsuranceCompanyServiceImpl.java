package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import com.ruoyi.business.service.IBizInsuranceCompanyService;

/**
 * 保险公司 服务实现
 */
@Service
public class BizInsuranceCompanyServiceImpl implements IBizInsuranceCompanyService
{
    @Autowired
    private BizInsuranceCompanyMapper companyMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public BizInsuranceCompany selectBizInsuranceCompanyById(Long id)
    {
        return companyMapper.selectBizInsuranceCompanyById(id);
    }

    @Override
    public BizInsuranceCompany selectBizInsuranceCompanyByAppKey(String appKey)
    {
        return companyMapper.selectBizInsuranceCompanyByAppKey(appKey);
    }

    @Override
    public BizInsuranceCompany selectBizInsuranceCompanyByUsername(String username)
    {
        return companyMapper.selectBizInsuranceCompanyByUsername(username);
    }

    @Override
    public int updateLoginInfo(BizInsuranceCompany company) { return companyMapper.updateBizInsuranceCompanyLoginInfo(company); }

    @Override
    public int deductBalance(Long companyId, BigDecimal amount)
    {
        return companyMapper.deductBalance(companyId, amount);
    }

    @Override
    public List<BizInsuranceCompany> selectBizInsuranceCompanyList(BizInsuranceCompany company)
    {
        return companyMapper.selectBizInsuranceCompanyList(company);
    }

    @Override
    @Transactional
    public int insertBizInsuranceCompany(BizInsuranceCompany company)
    {
        // 自动生成 AppKey 和 AppSecret
        if (company.getAppKey() == null || company.getAppKey().isEmpty())
        {
            company.setAppKey(generateAppKey());
        }
        if (company.getAppSecret() == null || company.getAppSecret().isEmpty())
        {
            company.setAppSecret(generateAppSecret());
        }
        // 默认余额为0
        if (company.getBalance() == null)
        {
            company.setBalance(BigDecimal.ZERO);
        }
        // 默认结算周期30天
        if (company.getBillingCycleDays() == null)
        {
            company.setBillingCycleDays(30);
        }
        // 默认状态正常
        if (company.getStatus() == null || company.getStatus().isEmpty())
        {
            company.setStatus("0");
        }
        // 密码加密
        if (company.getPassword() != null && !company.getPassword().isEmpty())
        {
            company.setPassword(passwordEncoder.encode(company.getPassword()));
        }
        return companyMapper.insertBizInsuranceCompany(company);
    }

    @Override
    public int updateBizInsuranceCompany(BizInsuranceCompany company)
    {
        if (company.getPassword() != null && !company.getPassword().isEmpty()
                && !company.getPassword().startsWith("$2"))
        {
            company.setPassword(passwordEncoder.encode(company.getPassword()));
        }
        return companyMapper.updateBizInsuranceCompany(company);
    }

    @Override
    public int deleteBizInsuranceCompanyByIds(Long[] ids)
    {
        return companyMapper.deleteBizInsuranceCompanyByIds(ids);
    }

    @Override
    public int changeStatus(Long id, String status)
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(id);
        company.setStatus(status);
        return companyMapper.updateBizInsuranceCompany(company);
    }

    @Override
    public int updatePassword(Long id, String password)
    {
        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setId(id);
        company.setPassword(passwordEncoder.encode(password));
        return companyMapper.updateBizInsuranceCompany(company);
    }

    /**
     * 生成32位 AppKey
     */
    private String generateAppKey()
    {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成64位 AppSecret
     */
    private String generateAppSecret()
    {
        return UUID.randomUUID().toString().replace("-", "")
             + UUID.randomUUID().toString().replace("-", "");
    }
}
