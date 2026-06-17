package com.ruoyi.web.controller.business;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.business.domain.BizHistoryMedicalData;
import com.ruoyi.business.domain.BizDataImportLog;
import com.ruoyi.business.service.IBizHistoryMedicalDataService;
import com.ruoyi.business.service.IBizDataImportService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.StringUtils;

@RestController
@RequestMapping("/business/data-import")
public class BizDataImportController extends BaseController
{
    @Autowired
    private IBizHistoryMedicalDataService historyMedicalDataService;

    @Autowired
    private IBizDataImportService dataImportService;

    @PreAuthorize("@ss.hasPermi('business:data-import:import')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            return AjaxResult.error(400, "请选择要导入的文件");
        }
        String batchNo = dataImportService.importExcel(file, getUsername());
        return AjaxResult.success("导入已提交", batchNo);
    }

    @PreAuthorize("@ss.hasPermi('business:data-import:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizDataImportLog param)
    {
        startPage();
        List<BizDataImportLog> list = dataImportService.selectList(param);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:data-import:detail')")
    @GetMapping("/detail")
    public AjaxResult detail(Long id)
    {
        return AjaxResult.success(dataImportService.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:data-import:delete')")
    @PostMapping("/delete")
    public AjaxResult delete(@RequestBody Map<String, Object> body)
    {
        String batchNo = str(body.get("batchNo"));
        if (StringUtils.isEmpty(batchNo))
        {
            return AjaxResult.error(400, "batchNo is required");
        }
        dataImportService.deleteByBatchNo(batchNo);
        return AjaxResult.success("删除成功");
    }

    @PreAuthorize("@ss.hasPermi('business:data-import:list')")
    @GetMapping("/history/list")
    public TableDataInfo historyList(BizHistoryMedicalData param)
    {
        startPage();
        List<BizHistoryMedicalData> list = historyMedicalDataService.selectList(param);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:data-import:list')")
    @GetMapping("/history/batch-nos")
    public AjaxResult batchNos()
    {
        return AjaxResult.success(historyMedicalDataService.selectDistinctBatchNo());
    }

    private String str(Object v)
    {
        return v == null ? null : String.valueOf(v).trim();
    }
}