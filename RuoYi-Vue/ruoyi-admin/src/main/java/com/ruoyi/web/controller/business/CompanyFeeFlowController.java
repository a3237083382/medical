package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizFeeFlow;
import com.ruoyi.business.service.IBizFeeFlowService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/api/fee-flow")
public class CompanyFeeFlowController extends BaseController
{
    @Autowired
    private IBizFeeFlowService feeFlowService;

    @GetMapping("/list")
    public AjaxResult list(BizFeeFlow flow, HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        flow.setCompanyId(companyId);
        List<BizFeeFlow> list = feeFlowService.selectBizFeeFlowList(flow);
        return AjaxResult.success(list);
    }
}
