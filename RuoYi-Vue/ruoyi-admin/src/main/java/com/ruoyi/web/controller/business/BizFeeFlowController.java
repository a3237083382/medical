package com.ruoyi.web.controller.business;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizFeeFlow;
import com.ruoyi.business.service.IBizFeeFlowService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/fee")
public class BizFeeFlowController extends BaseController
{
    @Autowired
    private IBizFeeFlowService feeFlowService;

    @PreAuthorize("@ss.hasPermi('business:fee:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizFeeFlow flow)
    {
        startPage();
        List<BizFeeFlow> list = feeFlowService.selectBizFeeFlowList(flow);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:fee:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(feeFlowService.selectBizFeeFlowById(id));
    }

    @Log(title = "扣费管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('business:fee:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizFeeFlow flow)
    {
        List<BizFeeFlow> list = feeFlowService.selectBizFeeFlowList(flow);
        ExcelUtil<BizFeeFlow> util = new ExcelUtil<BizFeeFlow>(BizFeeFlow.class);
        util.exportExcel(response, list, "扣费流水数据");
    }
}
