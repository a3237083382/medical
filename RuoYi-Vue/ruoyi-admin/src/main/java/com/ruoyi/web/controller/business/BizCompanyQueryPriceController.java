package com.ruoyi.web.controller.business;

import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.service.IBizQueryPriceService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/company-price")
public class BizCompanyQueryPriceController extends BaseController
{
    @Autowired
    private BizCompanyQueryPriceMapper companyPriceMapper;

    @Autowired
    private IBizQueryPriceService queryPriceService;

    @PreAuthorize("@ss.hasPermi('business:price:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCompanyQueryPrice price)
    {
        startPage();
        List<BizCompanyQueryPrice> list = companyPriceMapper.selectBizCompanyQueryPriceList(price);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:price:list')")
    @GetMapping("/company/{companyId}/items")
    public AjaxResult companyItems(@PathVariable Long companyId)
    {
        List<BizCompanyQueryPrice> result = new ArrayList<>();
        for (BizQueryPrice base : queryPriceService.selectBizQueryPriceList(new BizQueryPrice()))
        {
            BizCompanyQueryPrice companyPrice = companyPriceMapper.selectCompanyPrice(companyId, base.getQueryType());
            if (companyPrice == null)
            {
                companyPrice = new BizCompanyQueryPrice();
                companyPrice.setCompanyId(companyId);
                companyPrice.setQueryType(base.getQueryType());
                companyPrice.setQueryName(base.getQueryName());
                companyPrice.setHitFee(base.getFee());
                companyPrice.setNoResultFee(base.getFee());
                companyPrice.setStatus(base.getStatus() == null ? "0" : base.getStatus());
            }
            result.add(companyPrice);
        }
        return AjaxResult.success(result);
    }

    @Log(title = "公司接口价格", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('business:price:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizCompanyQueryPrice price)
    {
        List<BizCompanyQueryPrice> list = companyPriceMapper.selectBizCompanyQueryPriceList(price);
        ExcelUtil<BizCompanyQueryPrice> util = new ExcelUtil<>(BizCompanyQueryPrice.class);
        util.exportExcel(response, list, "公司接口价格");
    }

    @PreAuthorize("@ss.hasPermi('business:price:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(companyPriceMapper.selectBizCompanyQueryPriceById(id));
    }

    @Log(title = "公司接口价格", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('business:price:add')")
    @PostMapping
    public AjaxResult add(@RequestBody BizCompanyQueryPrice price)
    {
        return toAjax(companyPriceMapper.insertBizCompanyQueryPrice(price));
    }

    @Log(title = "公司接口价格", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:price:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody BizCompanyQueryPrice price)
    {
        return toAjax(companyPriceMapper.updateBizCompanyQueryPrice(price));
    }

    @Log(title = "公司接口价格", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('business:price:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(companyPriceMapper.deleteBizCompanyQueryPriceByIds(ids));
    }
}
