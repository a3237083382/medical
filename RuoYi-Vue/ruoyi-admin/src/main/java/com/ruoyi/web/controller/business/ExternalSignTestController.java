package com.ruoyi.web.controller.business;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.domain.AjaxResult;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/sign")
public class ExternalSignTestController
{
    @GetMapping("/test")
    public AjaxResult getTest(HttpServletRequest request)
    {
        return AjaxResult.success(context(request));
    }

    @PostMapping("/test")
    public AjaxResult postTest(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request)
    {
        Map<String, Object> result = context(request);
        result.put("body", body);
        return AjaxResult.success(result);
    }

    private Map<String, Object> context(HttpServletRequest request)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("companyId", request.getAttribute("companyId"));
        result.put("companyName", request.getAttribute("companyName"));
        return result;
    }
}
