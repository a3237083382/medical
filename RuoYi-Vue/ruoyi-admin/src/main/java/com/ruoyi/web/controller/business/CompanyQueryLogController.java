package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.service.IBizQueryLogService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/api/query-log")
public class CompanyQueryLogController extends BaseController
{
    @Autowired
    private IBizQueryLogService queryLogService;

    @GetMapping("/list")
    public AjaxResult list(BizQueryLog queryLog, HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        queryLog.setCompanyId(companyId);
        List<BizQueryLog> list = queryLogService.selectBizQueryLogList(queryLog);
        return AjaxResult.success(list);
    }
}
