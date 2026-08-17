package com.ruoyi.web.controller.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizDelayedQueryRequest;
import com.ruoyi.business.domain.BizDelayedQueryResult;
import com.ruoyi.business.domain.BizCompanyQueryPrice;
import com.ruoyi.business.domain.BizMonthlyUsage;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.domain.medical.MedicalQueryResult;
import com.ruoyi.business.domain.medical.MedicalQueryBatchValidationCommand;
import com.ruoyi.business.domain.medical.MedicalQueryBatchSubmission;
import com.ruoyi.business.domain.medical.MedicalQueryBatchRow;
import com.ruoyi.business.mapper.BizCompanyQueryPriceMapper;
import com.ruoyi.business.mapper.BizMonthlyUsageMapper;
import com.ruoyi.business.service.IBizQueryPriceService;
import com.ruoyi.business.service.IDelayedMedicalQueryService;
import com.ruoyi.business.service.IMedicalQueryService;
import com.ruoyi.business.service.IMedicalQueryBatchService;
import com.ruoyi.business.service.IMedicalQueryBatchSubmissionService;
import com.ruoyi.business.service.IMedicalQueryBatchCancellationService;
import com.ruoyi.business.service.IDelayedMedicalQueryExportService;
import com.ruoyi.business.service.IBizDelayedQueryService;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.business.domain.medical.MedicalQueryExportFile;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.core.CompanyEmbedRequestContext;

@Anonymous
@RestController
@RequestMapping("/company/embed/medical")
public class CompanyEmbedMedicalQueryController
{
    private static final String DELAYED_QUERY_TYPE = "precision_delayed";
    private static final BigDecimal NEAR_LIMIT_PERCENT = new BigDecimal("80");

    private final IBizQueryPriceService priceService;
    private final IMedicalQueryService medicalQueryService;
    private final IDelayedMedicalQueryService delayedMedicalQueryService;
    private IBizDelayedQueryService bizDelayedQueryService;
    private final IMedicalQueryBatchService medicalQueryBatchService;
    private final IMedicalQueryBatchSubmissionService medicalQueryBatchSubmissionService;
    private final IMedicalQueryBatchCancellationService medicalQueryBatchCancellationService;
    private IDelayedMedicalQueryExportService delayedMedicalQueryExportService;
    private final BizMonthlyUsageMapper monthlyUsageMapper;
    private final BizCompanyQueryPriceMapper companyPriceMapper;

    public CompanyEmbedMedicalQueryController(IBizQueryPriceService priceService,
            IMedicalQueryService medicalQueryService, IDelayedMedicalQueryService delayedMedicalQueryService,
            IMedicalQueryBatchService medicalQueryBatchService, BizMonthlyUsageMapper monthlyUsageMapper,
            BizCompanyQueryPriceMapper companyPriceMapper,
            IMedicalQueryBatchSubmissionService medicalQueryBatchSubmissionService,
            IMedicalQueryBatchCancellationService medicalQueryBatchCancellationService)
    {
        this.priceService = priceService;
        this.medicalQueryService = medicalQueryService;
        this.delayedMedicalQueryService = delayedMedicalQueryService;
        this.bizDelayedQueryService = null;
        this.medicalQueryBatchService = medicalQueryBatchService;
        this.medicalQueryBatchSubmissionService = medicalQueryBatchSubmissionService;
        this.medicalQueryBatchCancellationService = medicalQueryBatchCancellationService;
        this.monthlyUsageMapper = monthlyUsageMapper;
        this.companyPriceMapper = companyPriceMapper;
        this.delayedMedicalQueryExportService = null;
    }

    @Autowired
    public CompanyEmbedMedicalQueryController(IBizQueryPriceService priceService,
            IMedicalQueryService medicalQueryService, IDelayedMedicalQueryService delayedMedicalQueryService,
            IMedicalQueryBatchService medicalQueryBatchService, BizMonthlyUsageMapper monthlyUsageMapper,
            BizCompanyQueryPriceMapper companyPriceMapper,
            IMedicalQueryBatchSubmissionService medicalQueryBatchSubmissionService,
            IMedicalQueryBatchCancellationService medicalQueryBatchCancellationService,
            IDelayedMedicalQueryExportService delayedMedicalQueryExportService,
            IBizDelayedQueryService bizDelayedQueryService)
    {
        this(priceService, medicalQueryService, delayedMedicalQueryService, medicalQueryBatchService,
                monthlyUsageMapper, companyPriceMapper, medicalQueryBatchSubmissionService,
                medicalQueryBatchCancellationService);
        this.delayedMedicalQueryExportService = delayedMedicalQueryExportService;
        this.bizDelayedQueryService = bizDelayedQueryService;
    }

    public CompanyEmbedMedicalQueryController(IBizQueryPriceService priceService,
            IMedicalQueryService medicalQueryService, IDelayedMedicalQueryService delayedMedicalQueryService,
            IMedicalQueryBatchService medicalQueryBatchService, BizMonthlyUsageMapper monthlyUsageMapper,
            BizCompanyQueryPriceMapper companyPriceMapper,
            IMedicalQueryBatchSubmissionService medicalQueryBatchSubmissionService,
            IMedicalQueryBatchCancellationService medicalQueryBatchCancellationService,
            IDelayedMedicalQueryExportService delayedMedicalQueryExportService)
    {
        this(priceService, medicalQueryService, delayedMedicalQueryService, medicalQueryBatchService,
                monthlyUsageMapper, companyPriceMapper, medicalQueryBatchSubmissionService,
                medicalQueryBatchCancellationService, delayedMedicalQueryExportService, null);
    }

    @PostMapping(value = "/batches/import-preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult importBatchPreview(@RequestParam("file") MultipartFile file, HttpServletRequest request)
    {
        if (CompanyEmbedRequestContext.getCompany(request) == null)
        {
            return invalidAppKey();
        }
        try
        {
            return AjaxResult.success(medicalQueryBatchService.preview(file));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
        catch (IllegalArgumentException e)
        {
            return AjaxResult.error(400, e.getMessage());
        }
    }

    @PostMapping("/batches/validate")
    public AjaxResult validateBatch(@RequestBody MedicalQueryBatchValidationCommand command,
            HttpServletRequest request)
    {
        if (CompanyEmbedRequestContext.getCompany(request) == null)
        {
            return invalidAppKey();
        }
        try
        {
            return AjaxResult.success(medicalQueryBatchService.validate(command == null ? null : command.getRows()));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @PostMapping("/batches")
    public AjaxResult submitBatch(@RequestBody MedicalQueryBatchSubmission command, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            if (bizDelayedQueryService != null)
            {
                if (command == null)
                {
                    throw new IllegalArgumentException("名单不能为空");
                }
                var preview = medicalQueryBatchService.validate(command.getRows());
                if (preview.getInvalidCount() > 0)
                {
                    throw new IllegalArgumentException("名单中存在无效或重复记录");
                }
                List<BizDelayedQueryRequest> requests = new ArrayList<>();
                for (MedicalQueryBatchRow row : preview.getRows())
                {
                    BizDelayedQueryRequest item = new BizDelayedQueryRequest();
                    item.setPatientName(row.getName());
                    item.setIdCard(row.getIdCard());
                    requests.add(item);
                }
                List<BizDelayedQueryRequest> submitted = bizDelayedQueryService.submitBatch(company.getId(),
                        company.getCompanyName(), requests, request.getRemoteAddr());
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("batchNo", submitted.isEmpty() ? null : submitted.get(0).getBatchNo());
                result.put("items", submitted);
                return AjaxResult.success(result);
            }
            return AjaxResult.success(medicalQueryBatchSubmissionService.submit(company.getId(), command,
                    request.getRemoteAddr()));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
        catch (IllegalArgumentException e)
        {
            return AjaxResult.error(400, e.getMessage());
        }
    }

    @GetMapping("/batches/{batchNo}")
    public AjaxResult batchProgress(@PathVariable String batchNo, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            if (bizDelayedQueryService != null)
            {
                BizDelayedQueryRequest filter = new BizDelayedQueryRequest();
                filter.setCompanyId(company.getId());
                filter.setBatchNo(batchNo);
                List<BizDelayedQueryRequest> items = bizDelayedQueryService.selectList(filter);
                Map<String, Object> progress = new LinkedHashMap<>();
                int completed = 0;
                int processing = 0;
                int pending = 0;
                int failed = 0;
                int cancelled = 0;
                for (BizDelayedQueryRequest item : items)
                {
                    String status = item.getQueryStatus();
                    if ("QUERIED".equals(status)) completed++;
                    else if ("PROCESSING".equals(status)) processing++;
                    else if ("CANCELLED".equals(status)) cancelled++;
                    else if ("FAILED".equals(status)) failed++;
                    else pending++;
                }
                progress.put("batchNo", batchNo);
                progress.put("totalCount", items.size());
                progress.put("completedCount", completed);
                progress.put("processingCount", processing);
                progress.put("pendingCount", pending);
                progress.put("failedCount", failed);
                progress.put("cancelledCount", cancelled);
                progress.put("batchStatus", items.isEmpty() ? "NOT_FOUND"
                        : completed + failed + cancelled == items.size() ? "COMPLETED" : "PROCESSING");
                progress.put("items", items);
                return AjaxResult.success(progress);
            }
            return AjaxResult.success(medicalQueryBatchCancellationService.getProgress(company.getId(), batchNo));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @PostMapping("/batches/{batchNo}/cancel")
    public AjaxResult cancelBatch(@PathVariable String batchNo, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            if (bizDelayedQueryService != null)
            {
                return AjaxResult.success(bizDelayedQueryService.cancelBatch(company.getId(), batchNo));
            }
            return AjaxResult.success(medicalQueryBatchCancellationService.cancelBatch(company.getId(), batchNo));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @PostMapping("/batch-items/{itemId}/cancel")
    public AjaxResult cancelBatchItem(@PathVariable Long itemId, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            if (bizDelayedQueryService != null)
            {
                return AjaxResult.success(bizDelayedQueryService.cancelItem(company.getId(), itemId));
            }
            return AjaxResult.success(medicalQueryBatchCancellationService.cancelItem(company.getId(), itemId));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @PostMapping("/delayed/requests")
    public AjaxResult submitDelayed(@RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            if (bizDelayedQueryService != null)
            {
                return AjaxResult.success(toEmbedResponse(bizDelayedQueryService.submit(company.getId(),
                        company.getCompanyName(), toString(body.get("name")), toString(body.get("idCard")),
                        request.getRemoteAddr())));
            }
            return AjaxResult.success(delayedMedicalQueryService.submit(company.getId(), toString(body.get("name")),
                    toString(body.get("idCard")), request.getRemoteAddr()));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @GetMapping("/requests/{requestNo}")
    public AjaxResult requestDetail(@PathVariable String requestNo, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            if (bizDelayedQueryService != null)
            {
                BizDelayedQueryRequest detail = findBizRequest(company.getId(), requestNo);
                return detail == null ? AjaxResult.error(404, "请求不存在") : AjaxResult.success(toEmbedResponse(detail));
            }
            return AjaxResult.success(delayedMedicalQueryService.getRequest(company.getId(), requestNo));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @GetMapping("/history/requests")
    public AjaxResult requestHistory(@RequestParam(required = false) String requestNo,
            @RequestParam(required = false) String name, @RequestParam(required = false) String processStatus,
            @RequestParam(required = false) String resultStatus, @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            if (bizDelayedQueryService != null)
            {
                BizDelayedQueryRequest filter = new BizDelayedQueryRequest();
                filter.setCompanyId(company.getId());
                if (requestNo != null) filter.setRequestNo(requestNo);
                if (name != null) filter.setPatientName(name);
                if (processStatus != null && "COMPLETED".equals(processStatus)) filter.setQueryStatus("QUERIED");
                if (resultStatus != null) filter.setResultStatus(resultStatus);
                List<Map<String, Object>> history = new ArrayList<>();
                for (BizDelayedQueryRequest item : bizDelayedQueryService.selectList(filter))
                {
                    BizDelayedQueryRequest detail = bizDelayedQueryService.selectCompanyDetail(item.getId(), company.getId());
                    history.add(toEmbedResponse(detail == null ? item : detail));
                }
                return AjaxResult.success(history);
            }
            return AjaxResult.success(delayedMedicalQueryService.listHistory(company.getId(), requestNo, name,
                    processStatus, resultStatus, beginTime, endTime));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @GetMapping("/history/batches")
    public AjaxResult batchHistory(HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            return AjaxResult.success(delayedMedicalQueryService.listBatchHistory(company.getId()));
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @GetMapping("/notifications/unread")
    public AjaxResult unreadNotifications(HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("unreadCount", delayedMedicalQueryService.countUnread(company.getId()));
            return AjaxResult.success(data);
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @PostMapping("/requests/{requestNo}/read")
    public AjaxResult markRequestRead(@PathVariable String requestNo, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }
        try
        {
            delayedMedicalQueryService.markRead(company.getId(), requestNo);
            return AjaxResult.success("已标记为已查看");
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    @GetMapping("/requests/{requestNo}/export")
    public ResponseEntity<?> exportRequest(@PathVariable String requestNo, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return ResponseEntity.status(401).body(invalidAppKey());
        }
        try
        {
            return download(delayedMedicalQueryExportService.exportRequest(company.getId(), requestNo));
        }
        catch (MedicalQueryException e)
        {
            return ResponseEntity.status(errorHttpStatus(e)).body(queryError(e));
        }
    }

    @GetMapping("/batches/{batchNo}/export")
    public ResponseEntity<?> exportBatch(@PathVariable String batchNo, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return ResponseEntity.status(401).body(invalidAppKey());
        }
        try
        {
            return download(delayedMedicalQueryExportService.exportBatch(company.getId(), batchNo));
        }
        catch (MedicalQueryException e)
        {
            return ResponseEntity.status(errorHttpStatus(e)).body(queryError(e));
        }
    }

    private ResponseEntity<byte[]> download(MedicalQueryExportFile file)
    {
        String encoded = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded);
        headers.setContentLength(file.getContent().length);
        return ResponseEntity.ok().headers(headers).body(file.getContent());
    }

    private int errorHttpStatus(MedicalQueryException exception)
    {
        return switch (exception.getCode())
        {
            case "4041", "4042" -> 404;
            case "4092", "4093", "4094" -> 409;
            case "4000" -> 400;
            default -> 500;
        };
    }

    @GetMapping("/access")
    public AjaxResult access(HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }

        UsageSummary usage = loadUsage(company);
        boolean realtimeEnabled = !loadRealtimeQueryTypes(company.getId()).isEmpty();
        boolean delayedEnabled = isQueryTypeEnabled(company.getId(), DELAYED_QUERY_TYPE);

        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("singleRealtime", realtimeEnabled);
        capabilities.put("batchRealtime", realtimeEnabled);
        capabilities.put("singleDelayed", delayedEnabled);
        capabilities.put("batchDelayed", delayedEnabled);
        capabilities.put("singleExport", true);
        capabilities.put("batchExport", true);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("companyName", company.getCompanyName());
        data.put("companyCode", company.getCompanyCode());
        data.put("appKeyMasked", maskAppKey(company.getAppKey()));
        data.put("billingMonth", usage.billingMonth);
        data.put("budgetEnabled", usage.budgetEnabled);
        data.put("serviceStatus", usage.serviceStatus);
        data.put("capabilities", capabilities);
        return AjaxResult.success(data);
    }

    @GetMapping("/query-types")
    public AjaxResult queryTypes(HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }

        return AjaxResult.success(loadRealtimeQueryTypes(company.getId()));
    }

    @PostMapping("/query")
    public AjaxResult query(@RequestBody Map<String, Object> body, HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }

        String queryType = toString(body.get("queryType"));
        Map<String, Object> queryParams = toMap(body.get("queryParams"));
        if (StringUtils.isEmpty(queryType) || StringUtils.isEmpty(toString(queryParams.get("name")))
                || StringUtils.isEmpty(toString(queryParams.get("idCard"))))
        {
            return AjaxResult.error(400, "查询项目、姓名和身份证号不能为空")
                    .put("errorCode", "INVALID_PARAM");
        }

        MedicalQueryRequest queryRequest = new MedicalQueryRequest();
        queryRequest.setCompanyId(company.getId());
        queryRequest.setQueryType(queryType);
        queryRequest.setQueryParams(queryParams);
        queryRequest.setRequestIp(request.getRemoteAddr());
        try
        {
            MedicalQueryResult result = medicalQueryService.query(queryRequest);
            return AjaxResult.success(result);
        }
        catch (MedicalQueryException e)
        {
            return queryError(e);
        }
    }

    private String toString(Object value)
    {
        return value == null ? null : String.valueOf(value).trim();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value)
    {
        if (value instanceof Map<?, ?>)
        {
            return (Map<String, Object>) value;
        }
        return new LinkedHashMap<>();
    }

    private AjaxResult queryError(MedicalQueryException exception)
    {
        return switch (exception.getCode())
        {
            case "4000" -> AjaxResult.error(400, exception.getMessage()).put("errorCode", "INVALID_PARAM");
            case "4004" -> AjaxResult.error(400, exception.getMessage()).put("errorCode", "INVALID_FILE_TYPE");
            case "4005" -> AjaxResult.error(400, exception.getMessage()).put("errorCode", "BATCH_LIMIT_EXCEEDED");
            case "4006" -> AjaxResult.error(400, exception.getMessage()).put("errorCode", "VALIDATION_FAILED");
            case "4001" -> AjaxResult.error(402, exception.getMessage()).put("errorCode", "SERVICE_LIMIT_REACHED");
            case "4002" -> AjaxResult.error(403, exception.getMessage()).put("errorCode", "COMPANY_DISABLED");
            case "4003" -> AjaxResult.error(400, exception.getMessage()).put("errorCode", "QUERY_TYPE_DISABLED");
            case "4041" -> AjaxResult.error(404, exception.getMessage()).put("errorCode", "REQUEST_NOT_FOUND");
            case "4042" -> AjaxResult.error(404, exception.getMessage()).put("errorCode", "BATCH_NOT_FOUND");
            case "4092" -> AjaxResult.error(409, exception.getMessage()).put("errorCode", "NOT_CANCELLABLE");
            case "4093" -> AjaxResult.error(409, exception.getMessage()).put("errorCode", "RESULT_NOT_READY");
            case "4094" -> AjaxResult.error(409, exception.getMessage()).put("errorCode", "BATCH_NOT_FINISHED");
            case "5004" -> AjaxResult.error(500, exception.getMessage()).put("errorCode", "EXPORT_FAILED");
            case "5001", "5002", "5003" -> AjaxResult.error(503, exception.getMessage())
                    .put("errorCode", "SOURCE_UNAVAILABLE");
            default -> AjaxResult.error(500, "系统内部错误").put("errorCode", "INTERNAL_ERROR");
        };
    }

    @GetMapping("/usage")
    public AjaxResult usage(HttpServletRequest request)
    {
        BizInsuranceCompany company = CompanyEmbedRequestContext.getCompany(request);
        if (company == null)
        {
            return invalidAppKey();
        }

        UsageSummary usage = loadUsage(company);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("budget", usage.budget);
        data.put("usedAmount", usage.usedAmount);
        data.put("reservedAmount", usage.reservedAmount);
        data.put("remaining", usage.remaining);
        data.put("usagePercent", usage.usagePercent);
        data.put("billingMonth", usage.billingMonth);
        data.put("budgetEnabled", usage.budgetEnabled);
        data.put("serviceStatus", usage.serviceStatus);
        return AjaxResult.success(data);
    }

    private List<Map<String, Object>> loadRealtimeQueryTypes(Long companyId)
    {
        BizQueryPrice filter = new BizQueryPrice();
        filter.setStatus("0");
        List<Map<String, Object>> result = new ArrayList<>();
        for (BizQueryPrice basePrice : priceService.selectBizQueryPriceList(filter))
        {
            if (DELAYED_QUERY_TYPE.equals(basePrice.getQueryType()))
            {
                continue;
            }
            BizCompanyQueryPrice companyPrice = companyPriceMapper.selectCompanyPrice(companyId, basePrice.getQueryType());
            if (companyPrice != null && !"0".equals(companyPrice.getStatus()))
            {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("queryType", basePrice.getQueryType());
            item.put("queryName", companyPrice != null && StringUtils.isNotEmpty(companyPrice.getQueryName())
                    ? companyPrice.getQueryName() : basePrice.getQueryName());
            item.put("hitFee", companyPrice != null && companyPrice.getHitFee() != null
                    ? companyPrice.getHitFee() : nvl(basePrice.getFee()));
            item.put("noResultFee", BigDecimal.ZERO.setScale(2));
            result.add(item);
        }
        return result;
    }

    private boolean isQueryTypeEnabled(Long companyId, String queryType)
    {
        BizQueryPrice basePrice = priceService.selectBizQueryPriceByQueryType(queryType);
        if (basePrice == null || !"0".equals(basePrice.getStatus()))
        {
            return false;
        }
        BizCompanyQueryPrice companyPrice = companyPriceMapper.selectCompanyPrice(companyId, queryType);
        return companyPrice == null || "0".equals(companyPrice.getStatus());
    }

    private UsageSummary loadUsage(BizInsuranceCompany company)
    {
        String billingMonth = YearMonth.now().toString();
        boolean budgetEnabled = "0".equals(company.getBudgetEnabled()) && company.getMonthlyBudget() != null;
        BigDecimal budget = nvl(company.getMonthlyBudget());
        BizMonthlyUsage currentUsage = monthlyUsageMapper.selectUsage(company.getId(), billingMonth);
        BigDecimal usedAmount = currentUsage == null ? BigDecimal.ZERO : nvl(currentUsage.getUsedAmount());
        BigDecimal reservedAmount = currentUsage == null ? BigDecimal.ZERO : nvl(currentUsage.getReservedAmount());
        BigDecimal activeAmount = usedAmount.add(reservedAmount);
        BigDecimal remaining = budget.subtract(activeAmount).max(BigDecimal.ZERO);

        int usagePercent = 0;
        String serviceStatus = "NORMAL";
        if (budgetEnabled)
        {
            usagePercent = budget.signum() <= 0 ? 100
                    : activeAmount.multiply(new BigDecimal("100")).divide(budget, 0, RoundingMode.DOWN).min(new BigDecimal("100")).intValue();
            if ((currentUsage != null && !"0".equals(currentUsage.getStatus())) || activeAmount.compareTo(budget) >= 0)
            {
                serviceStatus = "LIMIT_REACHED";
            }
            else if (new BigDecimal(usagePercent).compareTo(NEAR_LIMIT_PERCENT) >= 0)
            {
                serviceStatus = "NEAR_LIMIT";
            }
        }
        return new UsageSummary(billingMonth, budgetEnabled, budget, usedAmount, reservedAmount, remaining,
                usagePercent, serviceStatus);
    }

    private BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String maskAppKey(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return "-";
        }
        if (value.length() <= 4)
        {
            return "****";
        }
        if (value.length() <= 8)
        {
            return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    private BizDelayedQueryRequest findBizRequest(Long companyId, String requestNo)
    {
        BizDelayedQueryRequest filter = new BizDelayedQueryRequest();
        filter.setCompanyId(companyId);
        filter.setRequestNo(requestNo);
        List<BizDelayedQueryRequest> requests = bizDelayedQueryService.selectList(filter);
        if (requests == null || requests.isEmpty())
        {
            return null;
        }
        BizDelayedQueryRequest detail = requests.get(0);
        return bizDelayedQueryService.selectCompanyDetail(detail.getId(), companyId);
    }

    private Map<String, Object> toEmbedResponse(BizDelayedQueryRequest request)
    {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("requestNo", request.getRequestNo());
        response.put("processStatus", "QUERIED".equals(request.getQueryStatus()) ? "COMPLETED" : request.getQueryStatus());
        response.put("resultStatus", request.getResultStatus());
        response.put("resultSummary", request.getResultMessage());
        response.put("resultVisible", "UPLOADED".equals(request.getUploadStatus()));
        if ("UPLOADED".equals(request.getUploadStatus()))
        {
            Map<String, Object> data = new LinkedHashMap<>();
            List<Map<String, Object>> records = new ArrayList<>();
            for (BizDelayedQueryResult result : request.getResults())
            {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("rowNo", result.getRowNo());
                row.put("rawJson", result.getRawJson());
                records.add(row);
            }
            data.put("records", records);
            response.put("data", data);
        }
        return response;
    }

    private AjaxResult invalidAppKey()
    {
        return AjaxResult.error(401, "AppKey缺失或无效").put("errorCode", "INVALID_APP_KEY");
    }

    private static class UsageSummary
    {
        private final String billingMonth;
        private final boolean budgetEnabled;
        private final BigDecimal budget;
        private final BigDecimal usedAmount;
        private final BigDecimal reservedAmount;
        private final BigDecimal remaining;
        private final int usagePercent;
        private final String serviceStatus;

        private UsageSummary(String billingMonth, boolean budgetEnabled, BigDecimal budget, BigDecimal usedAmount,
                BigDecimal reservedAmount, BigDecimal remaining, int usagePercent, String serviceStatus)
        {
            this.billingMonth = billingMonth;
            this.budgetEnabled = budgetEnabled;
            this.budget = budget;
            this.usedAmount = usedAmount;
            this.reservedAmount = reservedAmount;
            this.remaining = remaining;
            this.usagePercent = usagePercent;
            this.serviceStatus = serviceStatus;
        }
    }
}
