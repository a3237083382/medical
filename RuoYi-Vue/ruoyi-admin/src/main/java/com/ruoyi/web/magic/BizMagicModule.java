package com.ruoyi.web.magic;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.script.annotation.Comment;

import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.domain.medical.MedicalQueryResult;
import com.ruoyi.business.service.MedicalQueryException;
import com.ruoyi.business.service.impl.MedicalQueryServiceImpl;

@Component
@MagicModule("biz")
public class BizMagicModule
{
    private final MedicalQueryServiceImpl medicalQueryService;

    public BizMagicModule(MedicalQueryServiceImpl medicalQueryService)
    {
        this.medicalQueryService = medicalQueryService;
    }

    @Comment("query balance")
    public BigDecimal getBalance(@Comment("company id") Long companyId)
    {
        return medicalQueryService.getBalance(companyId);
    }

    @Comment("query price")
    public BigDecimal getQueryPrice(@Comment("query type") String queryType)
    {
        return medicalQueryService.getQueryPrice(queryType);
    }

    @Comment("deduct balance and write query log")
    public Map<String, Object> deductBalance(Long companyId, String queryType, Map<String, Object> queryParams, String requestIp)
    {
        return queryMedical(companyId, queryType, queryParams, requestIp);
    }

    @Comment("query medical data with billing")
    public Map<String, Object> queryMedical(Long companyId, String queryType, Map<String, Object> queryParams, String requestIp)
    {
        try
        {
            MedicalQueryRequest request = new MedicalQueryRequest();
            request.setCompanyId(companyId);
            request.setQueryType(queryType);
            request.setQueryParams(queryParams);
            request.setRequestIp(requestIp);
            return success(medicalQueryService.query(request));
        }
        catch (MedicalQueryException ex)
        {
            return error(ex.getCode(), ex.getMessage());
        }
    }

    private Map<String, Object> success(MedicalQueryResult result)
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", result.getCode());
        body.put("msg", result.getMsg());
        body.put("fee", result.getFee());
        body.put("balanceAfter", result.getBalanceAfter());
        body.put("data", result.getData());
        return body;
    }

    private Map<String, Object> error(String code, String message)
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("msg", message);
        body.put("data", null);
        return body;
    }
}
