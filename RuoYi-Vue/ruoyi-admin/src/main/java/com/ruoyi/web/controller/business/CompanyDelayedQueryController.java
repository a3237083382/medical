package com.ruoyi.web.controller.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizDelayedQueryRequest;
import com.ruoyi.business.service.IBizDelayedQueryService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;

@RestController
@RequestMapping("/company/api/delayed-query")
public class CompanyDelayedQueryController extends BaseController
{
    @Autowired
    private IBizDelayedQueryService delayedQueryService;

    @Log(title = "精准延时查询提交", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        String companyName = (String) request.getAttribute("companyName");
        return success(delayedQueryService.submit(companyId, companyName,
                str(body.get("patientName")), str(body.get("idCard")), request.getRemoteAddr()));
    }

    @Log(title = "精准延时查询批量提交", businessType = BusinessType.INSERT)
    @PostMapping("/submit-batch")
    public AjaxResult submitBatch(@RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        Long companyId = (Long) request.getAttribute("companyId");
        String companyName = (String) request.getAttribute("companyName");
        return success(delayedQueryService.submitBatch(companyId, companyName, rows(body), request.getRemoteAddr()));
    }

    @GetMapping("/list")
    public AjaxResult list(HttpServletRequest request)
    {
        BizDelayedQueryRequest filter = new BizDelayedQueryRequest();
        filter.setCompanyId((Long) request.getAttribute("companyId"));
        List<BizDelayedQueryRequest> list = delayedQueryService.selectList(filter);
        return success(list);
    }

    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id, HttpServletRequest request)
    {
        BizDelayedQueryRequest detail = delayedQueryService.selectCompanyDetail(id, (Long) request.getAttribute("companyId"));
        return detail == null ? AjaxResult.error(403, "forbidden") : success(detail);
    }

    private String str(Object value)
    {
        return value == null ? null : String.valueOf(value).trim();
    }

    private List<BizDelayedQueryRequest> rows(Map<String, Object> body)
    {
        List<BizDelayedQueryRequest> requests = new ArrayList<>();
        Object value = body.get("items");
        if (!(value instanceof List<?> list))
        {
            return requests;
        }
        for (Object item : list)
        {
            if (!(item instanceof Map<?, ?> map))
            {
                requests.add(null);
                continue;
            }
            BizDelayedQueryRequest queryRequest = new BizDelayedQueryRequest();
            queryRequest.setPatientName(str(first(map, "patientName", "name")));
            queryRequest.setIdCard(str(map.get("idCard")));
            requests.add(queryRequest);
        }
        return requests;
    }

    private Object first(Map<?, ?> map, String firstKey, String secondKey)
    {
        Object value = map.get(firstKey);
        return value == null ? map.get(secondKey) : value;
    }
}
