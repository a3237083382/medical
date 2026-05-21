package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizCompanyQueryPrice;

public interface BizCompanyQueryPriceMapper
{
    public BizCompanyQueryPrice selectBizCompanyQueryPriceById(Long id);

    public BizCompanyQueryPrice selectActivePrice(@Param("companyId") Long companyId, @Param("queryType") String queryType);

    public BizCompanyQueryPrice selectCompanyPrice(@Param("companyId") Long companyId, @Param("queryType") String queryType);

    public List<BizCompanyQueryPrice> selectBizCompanyQueryPriceList(BizCompanyQueryPrice price);

    public int insertBizCompanyQueryPrice(BizCompanyQueryPrice price);

    public int updateBizCompanyQueryPrice(BizCompanyQueryPrice price);

    public int deleteBizCompanyQueryPriceByIds(Long[] ids);
}
