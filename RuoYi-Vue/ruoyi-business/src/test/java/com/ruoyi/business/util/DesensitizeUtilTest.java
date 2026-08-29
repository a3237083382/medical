package com.ruoyi.business.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DesensitizeUtilTest
{
    @Test
    void desensitizeDeepMasksIdentityFieldsInsideRealtimeVisits()
    {
        Map<String, Object> visit = new LinkedHashMap<>();
        visit.put("身份证号码", "320683198312120713");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("visits", List.of(Map.of("basicInfo", List.of(visit))));

        Map<String, Object> result = DesensitizeUtil.desensitizeDeep(data);

        Map<?, ?> resultVisit = (Map<?, ?>) ((List<?>) result.get("visits")).get(0);
        Map<?, ?> basicInfo = (Map<?, ?>) ((List<?>) resultVisit.get("basicInfo")).get(0);
        assertEquals("320***********0713", basicInfo.get("身份证号码"));
    }
}
