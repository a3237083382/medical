package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizQueryPrice;

/**
 * 查询价目 Mapper接口
 */
public interface BizQueryPriceMapper
{
    public BizQueryPrice selectBizQueryPriceById(Long id);

    public BizQueryPrice selectBizQueryPriceByQueryType(String queryType);

    public List<BizQueryPrice> selectBizQueryPriceList(BizQueryPrice price);

    public int insertBizQueryPrice(BizQueryPrice price);

    public int updateBizQueryPrice(BizQueryPrice price);

    public int deleteBizQueryPriceByIds(Long[] ids);
}
