package com.ruoyi.business.config;

import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.mapper.BizInsuranceCompanyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始化测试数据（首次启动时自动创建测试保险公司）
 */
@Component
public class DataInitializer implements CommandLineRunner
{
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private BizInsuranceCompanyMapper companyMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args)
    {
        BizInsuranceCompany exist = companyMapper.selectBizInsuranceCompanyByUsername("test001");
        if (exist != null)
        {
            log.info("测试保险公司已存在，跳过初始化");
            return;
        }

        BizInsuranceCompany company = new BizInsuranceCompany();
        company.setCompanyName("测试保险公司");
        company.setCompanyCode("TEST001");
        company.setUsername("test001");
        company.setPassword(passwordEncoder.encode("123456"));
        company.setAppKey("a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6");
        company.setAppSecret("x1y2z3w4v5u6t7s8r9q0p1o2n3m4l5k6j7i8h7g6f5e4d3c2b1a0");
        company.setStatus("0");
        company.setContactPerson("张三");
        company.setContactPhone("13800138000");
        company.setRemark("系统自动创建的测试账号");
        company.setCreateBy("system");

        companyMapper.insertBizInsuranceCompany(company);
        log.info("测试保险公司创建成功：test001 / 123456");
    }
}
