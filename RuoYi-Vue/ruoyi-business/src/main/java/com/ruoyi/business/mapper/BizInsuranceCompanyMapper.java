package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizInsuranceCompany;

/**
 * 保险公司 Mapper接口
 */
public interface BizInsuranceCompanyMapper
{
    public BizInsuranceCompany selectBizInsuranceCompanyById(Long id);

    public BizInsuranceCompany selectBizInsuranceCompanyByIdForUpdate(Long id);

    public BizInsuranceCompany selectBizInsuranceCompanyByAppKey(String appKey);

    public BizInsuranceCompany selectBizInsuranceCompanyByUsername(String username);

    public int updateBizInsuranceCompanyLoginInfo(BizInsuranceCompany company);

    public int addBalance(@Param("companyId") Long companyId, @Param("amount") java.math.BigDecimal amount);

    public int deductBalance(@Param("companyId") Long companyId, @Param("amount") java.math.BigDecimal amount);

    public int settleBalance(@Param("companyId") Long companyId,
            @Param("amount") java.math.BigDecimal amount,
            @Param("balanceUpdateTime") java.util.Date balanceUpdateTime);

    public List<BizInsuranceCompany> selectBizInsuranceCompanyList(BizInsuranceCompany company);

    public int insertBizInsuranceCompany(BizInsuranceCompany company);

    public int updateBizInsuranceCompany(BizInsuranceCompany company);

    public int deleteBizInsuranceCompanyByIds(Long[] ids);
}
