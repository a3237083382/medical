package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizRechargeRequest;
import com.ruoyi.business.service.IBizRechargeRequestService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/api/recharge")
public class CompanyRechargeController extends BaseController
{
    @Autowired
    private IBizRechargeRequestService rechargeService;

    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody BizRechargeRequest request, HttpServletRequest req)
    {
        Long companyId = (Long) req.getAttribute("companyId");
        request.setCompanyId(companyId);
        request.setCreateBy(String.valueOf(companyId));
        return toAjax(rechargeService.insertBizRechargeRequest(request));
    }

    @GetMapping("/list")
    public AjaxResult list(HttpServletRequest req)
    {
        Long companyId = (Long) req.getAttribute("companyId");
        List<BizRechargeRequest> list = rechargeService.selectByCompanyId(companyId);
        return AjaxResult.success(list);
    }
}
