package com.ruoyi.web.controller.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.service.IBizQueryPriceService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/company/api/medical")
public class CompanyMedicalQueryController extends BaseController
{
    @Autowired
    private IBizQueryPriceService priceService;

    @GetMapping("/query-types")
    public AjaxResult queryTypes()
    {
        BizQueryPrice filter = new BizQueryPrice();
        filter.setStatus("0");
        return AjaxResult.success(priceService.selectBizQueryPriceList(filter));
    }
}
