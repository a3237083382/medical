package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizRechargeRequest;
import com.ruoyi.business.service.IBizRechargeRequestService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.domain.BizRechargeRequest;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/business/recharge")
public class BizRechargeRequestController extends BaseController
{
    @Autowired
    private IBizRechargeRequestService rechargeService;

    @PreAuthorize("@ss.hasPermi('business:recharge:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizRechargeRequest request)
    {
        startPage();
        List<BizRechargeRequest> list = rechargeService.selectBizRechargeRequestList(request);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:recharge:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(rechargeService.selectBizRechargeRequestById(id));
    }

    @Log(title = "充值审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:recharge:approve')")
    @PutMapping("/approve")
    public AjaxResult approve(@RequestBody BizRechargeRequest request, HttpServletRequest req)
    {
        return toAjax(rechargeService.approve(request.getId(), getUsername(), request.getReviewRemark()));
    }

    @Log(title = "充值审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:recharge:reject')")
    @PutMapping("/reject")
    public AjaxResult reject(@RequestBody BizRechargeRequest request, HttpServletRequest req)
    {
        return toAjax(rechargeService.reject(request.getId(), getUsername(), request.getReviewRemark()));
    }
}
