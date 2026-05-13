package com.ruoyi.web.controller.business;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.BizFeeFlow;
import com.ruoyi.business.domain.BizInsuranceCompany;
import com.ruoyi.business.domain.BizQueryLog;
import com.ruoyi.business.domain.BizQueryPrice;
import com.ruoyi.business.service.IBizFeeFlowService;
import com.ruoyi.business.service.IBizInsuranceCompanyService;
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
    private static final long SIGN_EXPIRE_MILLIS = 5 * 60 * 1000L;

    @Autowired
    private IBizInsuranceCompanyService companyService;

    @Autowired
    private IBizQueryPriceService priceService;

    @Autowired
    private IBizQueryLogService queryLogService;

    @Autowired
    private IBizFeeFlowService feeFlowService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/query")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult query(@RequestBody Map<String, String> params, HttpServletRequest request)
    {
        String body = toJson(params);
        BizInsuranceCompany company = authenticate(request, body);
        if (company == null)
        {
            return AjaxResult.error(401, "INVALID_SIGNATURE");
        }
        if (!"0".equals(company.getStatus()))
        {
            return AjaxResult.error(403, "COMPANY_DISABLED");
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
        BigDecimal before = company.getBalance() == null ? BigDecimal.ZERO : company.getBalance();
        int deducted = companyService.deductBalance(company.getId(), fee);
        if (deducted == 0)
        {
            recordLog(company.getId(), queryType, name, idCard, BigDecimal.ZERO, "1", "INSUFFICIENT_BALANCE");
            return AjaxResult.error(402, "INSUFFICIENT_BALANCE");
        }

        BizQueryLog log = recordLog(company.getId(), queryType, name, idCard, fee, "0", "SUCCESS");
        BizInsuranceCompany updated = companyService.selectBizInsuranceCompanyById(company.getId());
        recordFeeFlow(company.getId(), fee, before, updated.getBalance(), log.getId());
        return AjaxResult.success("SUCCESS", buildResult(price, name, idCard));
    }

    private BizInsuranceCompany authenticate(HttpServletRequest request, String body)
    {
        String appKey = trim(request.getHeader("X-App-Key"));
        String timestamp = trim(request.getHeader("X-Timestamp"));
        String nonce = trim(request.getHeader("X-Nonce"));
        String sign = trim(request.getHeader("X-Sign"));
        if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(timestamp)
                || StringUtils.isEmpty(nonce) || StringUtils.isEmpty(sign))
        {
            return null;
        }
        if (!isValidTimestamp(timestamp))
        {
            return null;
        }

        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey);
        if (company == null || StringUtils.isEmpty(company.getAppSecret()))
        {
            return null;
        }

        String expected = sha256(appKey + timestamp + nonce + body + company.getAppSecret());
        return sign.equalsIgnoreCase(expected) ? company : null;
    }

    private boolean isValidTimestamp(String timestamp)
    {
        try
        {
            long value = Long.parseLong(timestamp);
            return Math.abs(System.currentTimeMillis() - value) <= SIGN_EXPIRE_MILLIS;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
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

    private void recordFeeFlow(Long companyId, BigDecimal fee, BigDecimal before, BigDecimal after, Long queryLogId)
    {
        BizFeeFlow flow = new BizFeeFlow();
        flow.setCompanyId(companyId);
        flow.setOperationType("SETTLEMENT");
        flow.setAmount(fee.negate());
        flow.setBalanceBefore(before);
        flow.setBalanceAfter(after);
        flow.setOperator("OPEN_API");
        flow.setBizId(queryLogId);
        flow.setRemark("QUERY_FEE");
        feeFlowService.insertBizFeeFlow(flow);
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

    private String sha256(String value)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes)
            {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private String trim(String value)
    {
        return value == null ? null : value.trim();
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
