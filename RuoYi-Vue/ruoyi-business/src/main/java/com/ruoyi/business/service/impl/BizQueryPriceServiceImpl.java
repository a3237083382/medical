package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.mapper.BizQueryPriceMapper;
import com.ruoyi.business.service.IBizQueryPriceService;

/**
 * 查询价目 服务实现
 */
@Service
public class BizQueryPriceServiceImpl implements IBizQueryPriceService
{
    @Autowired
    private BizQueryPriceMapper priceMapper;

    @Override
    public BizQueryPrice selectBizQueryPriceById(Long id)
    {
        return priceMapper.selectBizQueryPriceById(id);
    }

    @Override
    public BizQueryPrice selectBizQueryPriceByQueryType(String queryType)
    {
        return priceMapper.selectBizQueryPriceByQueryType(queryType);
    }

    @Override
    public List<BizQueryPrice> selectBizQueryPriceList(BizQueryPrice price)
    {
        return priceMapper.selectBizQueryPriceList(price);
    }

    @Override
    public int insertBizQueryPrice(BizQueryPrice price)
    {
        if (price.getStatus() == null || price.getStatus().isEmpty())
        {
            price.setStatus("0");
        }
        return priceMapper.insertBizQueryPrice(price);
    }

    @Override
    public int updateBizQueryPrice(BizQueryPrice price)
    {
        return priceMapper.updateBizQueryPrice(price);
    }

    @Override
    public int deleteBizQueryPriceByIds(Long[] ids)
    {
        return priceMapper.deleteBizQueryPriceByIds(ids);
    }
}
