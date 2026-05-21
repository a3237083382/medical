package com.ruoyi.web.controller.business;

import java.time.YearMonth;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizMonthlyBill;
import com.ruoyi.business.domain.BizMonthlyBillDetail;
import com.ruoyi.business.mapper.BizMonthlyBillDetailMapper;
import com.ruoyi.business.mapper.BizMonthlyBillMapper;
import com.ruoyi.business.service.impl.BillingSettlementService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/business/monthly-bill")
public class BizMonthlyBillController extends BaseController
{
    @Autowired
    private BizMonthlyBillMapper monthlyBillMapper;

    @Autowired
    private BizMonthlyBillDetailMapper monthlyBillDetailMapper;

    @Autowired
    private BillingSettlementService billingSettlementService;

    @PreAuthorize("@ss.hasPermi('business:fee:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizMonthlyBill bill)
    {
        startPage();
        List<BizMonthlyBill> list = monthlyBillMapper.selectBizMonthlyBillList(bill);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:fee:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        BizMonthlyBill bill = monthlyBillMapper.selectBizMonthlyBillById(id);
        BizMonthlyBillDetail filter = new BizMonthlyBillDetail();
        filter.setBillId(id);
        AjaxResult result = success(bill);
        result.put("details", monthlyBillDetailMapper.selectBizMonthlyBillDetailList(filter));
        return result;
    }

    @PreAuthorize("@ss.hasPermi('business:fee:list')")
    @PostMapping("/generate/{billingMonth}")
    public AjaxResult generate(@PathVariable String billingMonth)
    {
        return success(billingSettlementService.generateMonthlyBills(normalizeMonth(billingMonth)));
    }

    private String normalizeMonth(String billingMonth)
    {
        if (billingMonth == null || billingMonth.isEmpty())
        {
            return YearMonth.now().minusMonths(1).toString();
        }
        return billingMonth;
    }
}
