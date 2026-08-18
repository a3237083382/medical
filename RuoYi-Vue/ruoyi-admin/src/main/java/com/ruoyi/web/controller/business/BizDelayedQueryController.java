package com.ruoyi.web.controller.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizDelayedQueryRequest;
import com.ruoyi.business.domain.BizDelayedQueryResult;
import com.ruoyi.business.service.IBizDelayedQueryService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

@RestController
@RequestMapping("/business/delayed-query")
public class BizDelayedQueryController extends BaseController
{
    private static final String COVERAGE_RECORD_TYPE = "INSURANCE_COVERAGE";

    @Autowired
    private IBizDelayedQueryService delayedQueryService;

    @PreAuthorize("@ss.hasPermi('business:delayed-query:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizDelayedQueryRequest request)
    {
        startPage();
        return getDataTable(delayedQueryService.selectList(request));
    }

    @PreAuthorize("@ss.hasPermi('business:delayed-query:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(delayedQueryService.selectAdminDetail(id));
    }

    @Log(title = "精准延时查询保存草稿", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:delayed-query:edit')")
    @PostMapping("/{id}/save")
    public AjaxResult save(@PathVariable Long id, @RequestBody Map<String, Object> body)
    {
        return success(delayedQueryService.saveDraft(id, allRows(body), str(body.get("resultStatus")),
                str(body.get("resultMessage")), getUsername()));
    }

    @Log(title = "精准延时查询上传完毕", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:delayed-query:edit')")
    @PostMapping("/{id}/complete")
    public AjaxResult complete(@PathVariable Long id, @RequestBody Map<String, Object> body)
    {
        return success(delayedQueryService.complete(id, allRows(body), str(body.get("resultStatus")),
                str(body.get("resultMessage")), getUsername()));
    }

    @Log(title = "精准延时查询修改结果", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:delayed-query:edit')")
    @PutMapping("/{id}/result")
    public AjaxResult updateResult(@PathVariable Long id, @RequestBody Map<String, Object> body)
    {
        return success(delayedQueryService.updateUploadedResult(id, allRows(body), str(body.get("resultStatus")),
                str(body.get("resultMessage")), getUsername(), str(body.get("modifyReason"))));
    }

    @Log(title = "精准延时查询导入Excel", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('business:delayed-query:edit')")
    @PostMapping("/{id}/import")
    public AjaxResult importExcel(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception
    {
        try
        {
            return success(delayedQueryService.importExcel(id, file, getUsername()));
        }
        catch (IllegalArgumentException e)
        {
            return AjaxResult.error(400, e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('business:delayed-query:list')")
    @GetMapping("/company/{companyId}/logs")
    public AjaxResult companyLogs(@PathVariable Long companyId)
    {
        return success(delayedQueryService.selectCompanyLogs(companyId));
    }

    @SuppressWarnings("unchecked")
    private List<BizDelayedQueryResult> rows(Map<String, Object> body)
    {
        List<BizDelayedQueryResult> results = new ArrayList<>();
        Object value = body.get("results");
        if (!(value instanceof List<?> list))
        {
            return results;
        }
        for (Object item : list)
        {
            BizDelayedQueryResult result = new BizDelayedQueryResult();
            if (item instanceof Map<?, ?> map)
            {
                Object rawJson = map.get("rawJson");
                if (rawJson != null)
                {
                    result.setRawJson(str(rawJson));
                }
                else
                {
                    result.setRawJson(JSON.toJSONString((Map<String, Object>) item));
                }
            }
            else if (item != null)
            {
                result.setRawJson(str(item));
            }
            results.add(result);
        }
        return results;
    }

    private List<BizDelayedQueryResult> allRows(Map<String, Object> body)
    {
        List<BizDelayedQueryResult> results = rows(body);
        Object value = body.get("insuranceCoverage");
        if (!(value instanceof List<?> list))
        {
            return results;
        }
        for (Object item : list)
        {
            if (!(item instanceof Map<?, ?> source))
            {
                continue;
            }
            boolean hasContent = source.values().stream()
                    .anyMatch(fieldValue -> fieldValue != null && !String.valueOf(fieldValue).trim().isEmpty());
            if (!hasContent)
            {
                continue;
            }
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("__recordType", COVERAGE_RECORD_TYPE);
            source.forEach((key, fieldValue) -> row.put(String.valueOf(key), fieldValue));
            BizDelayedQueryResult result = new BizDelayedQueryResult();
            result.setRawJson(JSON.toJSONString(row));
            results.add(result);
        }
        return results;
    }

    private String str(Object value)
    {
        return value == null ? null : String.valueOf(value).trim();
    }
}
