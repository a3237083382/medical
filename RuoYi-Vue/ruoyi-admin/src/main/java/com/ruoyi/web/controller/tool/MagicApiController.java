package com.ruoyi.web.controller.tool;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.web.magic.BizMagicModule;

@RestController
@RequestMapping("/magic/api")
public class MagicApiController {

    private final BizMagicModule bizMagicModule;

    public MagicApiController(BizMagicModule bizMagicModule) {
        this.bizMagicModule = bizMagicModule;
    }

    @GetMapping("/test/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping("/external/medical/query")
    public Map<String, Object> queryMedical(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long companyId = toLong(body.get("companyId"));
        String queryType = toString(body.get("queryType"));
        Map<String, Object> queryParams = toMap(body.get("queryParams"));
        return bizMagicModule.queryMedical(companyId, queryType, queryParams, request.getRemoteAddr());
    }

    @GetMapping("/external/balance/query")
    public Map<String, Object> queryBalance(@RequestParam Long companyId) {
        Map<String, Object> result = new LinkedHashMap<>();
        BigDecimal balance = bizMagicModule.getBalance(companyId);
        result.put("code", "0");
        result.put("msg", "success");
        result.put("balance", balance);
        return result;
    }

    @GetMapping("/external/price/query")
    public Map<String, Object> queryPrice(@RequestParam String queryType) {
        Map<String, Object> result = new LinkedHashMap<>();
        BigDecimal fee = bizMagicModule.getQueryPrice(queryType);
        result.put("code", "0");
        result.put("msg", "success");
        result.put("queryType", queryType);
        result.put("fee", fee);
        return result;
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isEmpty()) {
            return Long.valueOf(text);
        }
        return null;
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        if (value instanceof Map<?, ?>) {
            return (Map<String, Object>) value;
        }
        return new LinkedHashMap<>();
    }
}
