package com.ruoyi.web.controller.business;

import java.util.List;
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
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.medical.DelayedMedicalQueryResultCommand;
import com.ruoyi.business.service.IDelayedMedicalQueryAdminService;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

@RestController
@RequestMapping("/business/delayed-query")
public class DelayedMedicalQueryAdminController extends BaseController
{
    private final IDelayedMedicalQueryAdminService delayedQueryService;

    public DelayedMedicalQueryAdminController(IDelayedMedicalQueryAdminService delayedQueryService)
    {
        this.delayedQueryService = delayedQueryService;
    }

    @PreAuthorize("@ss.hasPermi('business:delayed-query:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizMedicalQueryRequest request)
    {
        startPage();
        List<BizMedicalQueryRequest> list = delayedQueryService.selectList(request);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:delayed-query:list')")
    @GetMapping("/batches/list")
    public TableDataInfo batchList(BizMedicalQueryBatch batch)
    {
        startPage();
        return getDataTable(delayedQueryService.selectBatchList(batch));
    }

    @PreAuthorize("@ss.hasPermi('business:delayed-query:query')")
    @GetMapping("/batches/{id}")
    public AjaxResult getBatchInfo(@PathVariable Long id)
    {
        try
        {
            return success(delayedQueryService.getBatchDetail(id));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @PreAuthorize("@ss.hasPermi('business:delayed-query:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        try
        {
            return success(delayedQueryService.getDetail(id));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @Log(title = "精准延时查询", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:delayed-query:start')")
    @PostMapping("/{id}/start")
    public AjaxResult start(@PathVariable Long id)
    {
        try
        {
            delayedQueryService.start(id);
            return success("已开始处理");
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @PreAuthorize("@ss.hasPermi('business:delayed-query:edit')")
    @PostMapping("/{id}/result/import-preview")
    public AjaxResult importPreview(@PathVariable Long id, @RequestParam("file") MultipartFile file)
    {
        try
        {
            return success(delayedQueryService.previewExcel(id, file));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @Log(title = "精准延时结果草稿", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:delayed-query:edit')")
    @PutMapping("/{id}/result/draft")
    public AjaxResult saveDraft(@PathVariable Long id,
            @RequestBody DelayedMedicalQueryResultCommand command)
    {
        try
        {
            delayedQueryService.saveDraft(id, command, getUsername());
            return success("草稿已保存");
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @Log(title = "精准延时结果上传", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:delayed-query:complete')")
    @PostMapping("/{id}/complete")
    public AjaxResult complete(@PathVariable Long id,
            @RequestBody DelayedMedicalQueryResultCommand command)
    {
        try
        {
            delayedQueryService.complete(id, command, getUsername());
            return success("结果已上传");
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @Log(title = "精准延时结果修改", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('business:delayed-query:update')")
    @PutMapping("/{id}/result")
    public AjaxResult updateResult(@PathVariable Long id,
            @RequestBody DelayedMedicalQueryResultCommand command)
    {
        try
        {
            delayedQueryService.updateUploaded(id, command, getUsername());
            return success("结果已修改");
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    private AjaxResult queryError(MedicalQueryException exception)
    {
        return switch (exception.getCode())
        {
            case "4000" -> AjaxResult.error(400, exception.getMessage()).put("errorCode", "INVALID_PARAM");
            case "4001" -> AjaxResult.error(402, exception.getMessage()).put("errorCode", "SERVICE_LIMIT_REACHED");
            case "4041" -> AjaxResult.error(404, exception.getMessage()).put("errorCode", "REQUEST_NOT_FOUND");
            case "4042" -> AjaxResult.error(404, exception.getMessage()).put("errorCode", "BATCH_NOT_FOUND");
            case "4091" -> AjaxResult.error(409, exception.getMessage()).put("errorCode", "INVALID_STATE");
            default -> AjaxResult.error(500, "系统内部错误").put("errorCode", "INTERNAL_ERROR");
        };
    }
}
