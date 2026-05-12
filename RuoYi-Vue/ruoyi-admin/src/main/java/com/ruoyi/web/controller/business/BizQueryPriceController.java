package com.ruoyi.web.controller.business;

import java.util.List;
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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.service.IBizQueryPriceService;

/**
 * 查询价目 Controller
 */
@RestController
@RequestMapping("/business/price")
public class BizQueryPriceController extends BaseController
{
    @Autowired
    private IBizQueryPriceService priceService;

    /**
     * 查询价目列表
     */
    @PreAuthorize("@ss.hasPermi('business:price:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizQueryPrice price)
    {
        startPage();
        List<BizQueryPrice> list = priceService.selectBizQueryPriceList(price);
        return getDataTable(list);
    }

    /**
     * 导出查询价目列表
     */
    @Log(title = "查询价目管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('business:price:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizQueryPrice price)
    {
        List<BizQueryPrice> list = priceService.selectBizQueryPriceList(price);
        ExcelUtil<BizQueryPrice> util = new ExcelUtil<BizQueryPrice>(BizQueryPrice.class);
        util.exportExcel(response, list, "查询价目数据");
    }

    /**
     * 获取查询价目详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:price:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(priceService.selectBizQueryPriceById(id));
    }

    /**
     * 新增查询价目
     */
    @Log(title = "查询价目管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('business:price:add')")
    @PostMapping
    public AjaxResult add(@RequestBody BizQueryPrice price)
    {
        return toAjax(priceService.insertBizQueryPrice(price));
    }

    /**
     * 修改查询价目
     */
    @Log(title = "查询价目管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:price:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody BizQueryPrice price)
    {
        return toAjax(priceService.updateBizQueryPrice(price));
    }

    /**
     * 删除查询价目
     */
    @Log(title = "查询价目管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('business:price:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(priceService.deleteBizQueryPriceByIds(ids));
    }
}
