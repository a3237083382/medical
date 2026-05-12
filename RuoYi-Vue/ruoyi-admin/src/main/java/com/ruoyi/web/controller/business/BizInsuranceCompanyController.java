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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.service.IBizInsuranceCompanyService;

/**
 * 保险公司 Controller
 */
@RestController
@RequestMapping("/business/company")
public class BizInsuranceCompanyController extends BaseController
{
    @Autowired
    private IBizInsuranceCompanyService companyService;

    /**
     * 查询保险公司列表
     */
    @PreAuthorize("@ss.hasPermi('business:company:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizInsuranceCompany company)
    {
        startPage();
        List<BizInsuranceCompany> list = companyService.selectBizInsuranceCompanyList(company);
        return getDataTable(list);
    }

    /**
     * 导出保险公司列表
     */
    @Log(title = "保险公司管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('business:company:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizInsuranceCompany company)
    {
        List<BizInsuranceCompany> list = companyService.selectBizInsuranceCompanyList(company);
        ExcelUtil<BizInsuranceCompany> util = new ExcelUtil<BizInsuranceCompany>(BizInsuranceCompany.class);
        util.exportExcel(response, list, "保险公司数据");
    }

    /**
     * 获取保险公司详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:company:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(companyService.selectBizInsuranceCompanyById(id));
    }

    /**
     * 新增保险公司
     */
    @Log(title = "保险公司管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('business:company:add')")
    @PostMapping
    public AjaxResult add(@RequestBody BizInsuranceCompany company)
    {
        return toAjax(companyService.insertBizInsuranceCompany(company));
    }

    /**
     * 修改保险公司
     */
    @Log(title = "保险公司管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:company:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody BizInsuranceCompany company)
    {
        return toAjax(companyService.updateBizInsuranceCompany(company));
    }

    /**
     * 删除保险公司
     */
    @Log(title = "保险公司管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('business:company:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(companyService.deleteBizInsuranceCompanyByIds(ids));
    }

    /**
     * 启用/停用
     */
    @Log(title = "保险公司状态变更", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:company:edit')")
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestParam Long id, @RequestParam String status)
    {
        return toAjax(companyService.changeStatus(id, status));
    }
}
