package com.ruoyi.web.controller.business;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.business.service.IBizQueryLogService;
import com.ruoyi.business.service.IBizQueryPriceService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import jakarta.servlet.http.HttpServletRequest;

@Anonymous
@RestController
@RequestMapping("/open/api/medical")
public class OpenMedicalQueryController
{

    @Autowired
    private IBizInsuranceCompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IBizQueryPriceService priceService;

    @Autowired
    private IBizQueryLogService queryLogService;


    @PostMapping("/query")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult query(@RequestBody Map<String, String> params, HttpServletRequest request)
    {
        BizInsuranceCompany company = resolveCompany(request);
        if (company == null)
        {
            return AjaxResult.error(401, "INVALID_SIGNATURE");
        }
        if (!"0".equals(company.getStatus()))
        {
            return AjaxResult.error(403, "COMPANY_DISABLED");
        }
        if (isNegative(company.getBalance()))
        {
            return AjaxResult.error(402, "INSUFFICIENT_BALANCE");
        }

        String queryType = trim(params.get("queryType"));
        String name = trim(params.get("name"));
        String idCard = trim(params.get("idCard"));
        if (StringUtils.isEmpty(queryType) || StringUtils.isEmpty(name) || StringUtils.isEmpty(idCard))
        {
            recordLog(company.getId(), queryType, name, idCard, BigDecimal.ZERO, "1", "INVALID_PARAM");
            return AjaxResult.error(400, "INVALID_PARAM");
        }

        BizQueryPrice price = priceService.selectBizQueryPriceByQueryType(queryType);
        if (price == null || !"0".equals(price.getStatus()))
        {
            recordLog(company.getId(), queryType, name, idCard, BigDecimal.ZERO, "1", "QUERY_TYPE_DISABLED");
            return AjaxResult.error(400, "QUERY_TYPE_DISABLED");
        }

        BigDecimal fee = price.getFee() == null ? BigDecimal.ZERO : price.getFee();
        recordLog(company.getId(), queryType, name, idCard, fee, "0", "SUCCESS");
        return AjaxResult.success("SUCCESS", buildResult(price, name, idCard));
    }



    private BizQueryLog recordLog(Long companyId, String queryType, String name, String idCard,
            BigDecimal fee, String status, String remark)
    {
        BizQueryLog log = new BizQueryLog();
        log.setCompanyId(companyId);
        log.setQueryType(StringUtils.isEmpty(queryType) ? "UNKNOWN" : queryType);
        log.setQueryParams(toJson(buildMaskedParams(name, idCard)));
        log.setFee(fee == null ? BigDecimal.ZERO : fee);
        log.setStatus(status);
        log.setRequestTime(new Date());
        log.setRequestIp(IpUtils.getIpAddr());
        log.setRemark(remark);
        queryLogService.insertBizQueryLog(log);
        return log;
    }

    private Map<String, Object> buildResult(BizQueryPrice price, String name, String idCard)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("queryType", price.getQueryType());
        result.put("queryName", price.getQueryName());
        result.put("fee", price.getFee());
        result.put("name", maskName(name));
        result.put("idCard", maskIdCard(idCard));
        result.put("queryTime", new Date());
        result.put("summary", "No high-risk medical record found in sample data.");
        result.put("records", Collections.emptyList());
        return result;
    }

    private Map<String, String> buildMaskedParams(String name, String idCard)
    {
        Map<String, String> data = new HashMap<>();
        data.put("name", maskName(name));
        data.put("idCard", maskIdCard(idCard));
        return data;
    }

    private BizInsuranceCompany resolveCompany(HttpServletRequest request)
    {
        String appKey = trim(request.getHeader("X-App-Key"));
        if (StringUtils.isEmpty(appKey))
        {
            return null;
        }
        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey);
        if (company == null || !"0".equals(company.getStatus()))
        {
            return null;
        }
        return company;
    }

    private String toJson(Object value)
    {
        try
        {
            return objectMapper.writeValueAsString(value);
        }
        catch (JsonProcessingException e)
        {
            return "{}";
        }
    }


    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    private boolean isNegative(BigDecimal value)
    {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    private String maskName(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return "";
        }
        return value.length() <= 1 ? "*" : value.substring(0, 1) + "*";
    }

    private String maskIdCard(String value)
    {
        if (StringUtils.isEmpty(value) || value.length() < 8)
        {
            return "****";
        }
        return value.substring(0, 4) + "**********" + value.substring(value.length() - 4);
    }
}
