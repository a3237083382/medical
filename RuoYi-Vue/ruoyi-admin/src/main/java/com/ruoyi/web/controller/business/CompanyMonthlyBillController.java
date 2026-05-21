package com.ruoyi.web.controller.business;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizMonthlyBill;
import com.ruoyi.business.domain.BizMonthlyBillDetail;
import com.ruoyi.business.mapper.BizMonthlyBillDetailMapper;
import com.ruoyi.business.mapper.BizMonthlyBillMapper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/api/monthly-bill")
public class CompanyMonthlyBillController extends BaseController
{
    @Autowired
    private BizMonthlyBillMapper monthlyBillMapper;

    @Autowired
    private BizMonthlyBillDetailMapper monthlyBillDetailMapper;

    @GetMapping("/list")
    public AjaxResult list(BizMonthlyBill bill, HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        bill.setCompanyId(companyId);
        return AjaxResult.success(monthlyBillMapper.selectBizMonthlyBillList(bill));
    }

    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id, HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        BizMonthlyBill bill = monthlyBillMapper.selectBizMonthlyBillById(id);
        if (bill == null || !companyId.equals(bill.getCompanyId()))
        {
            return AjaxResult.error(404, "bill not found");
        }
        BizMonthlyBillDetail filter = new BizMonthlyBillDetail();
        filter.setBillId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("bill", bill);
        data.put("details", monthlyBillDetailMapper.selectBizMonthlyBillDetailList(filter));
        return AjaxResult.success(data);
    }
}
