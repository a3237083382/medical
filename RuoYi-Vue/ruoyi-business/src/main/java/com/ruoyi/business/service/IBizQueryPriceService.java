package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizQueryPrice;

/**
 * 查询价目 服务层
 */
public interface IBizQueryPriceService
{
    public BizQueryPrice selectBizQueryPriceById(Long id);

    public BizQueryPrice selectBizQueryPriceByQueryType(String queryType);

    public List<BizQueryPrice> selectBizQueryPriceList(BizQueryPrice price);

    public int insertBizQueryPrice(BizQueryPrice price);

    public int updateBizQueryPrice(BizQueryPrice price);

    public int deleteBizQueryPriceByIds(Long[] ids);
}
