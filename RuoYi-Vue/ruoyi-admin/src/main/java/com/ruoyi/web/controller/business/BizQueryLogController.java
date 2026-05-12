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
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.service.IBizQueryLogService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/log")
public class BizQueryLogController extends BaseController
{
    @Autowired
    private IBizQueryLogService queryLogService;

    @PreAuthorize("@ss.hasPermi('business:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizQueryLog queryLog)
    {
        startPage();
        List<BizQueryLog> list = queryLogService.selectBizQueryLogList(queryLog);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:log:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(queryLogService.selectBizQueryLogById(id));
    }

    @Log(title = "查询日志管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('business:log:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizQueryLog queryLog)
    {
        List<BizQueryLog> list = queryLogService.selectBizQueryLogList(queryLog);
        ExcelUtil<BizQueryLog> util = new ExcelUtil<BizQueryLog>(BizQueryLog.class);
        util.exportExcel(response, list, "查询日志数据");
    }
}
