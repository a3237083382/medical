package com.ruoyi.business.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DesensitizeUtil
{
    private DesensitizeUtil()
    {
    }

    public static Map<String, Object> desensitize(Map<String, Object> data)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        if (data == null)
        {
            return result;
        }
        for (Map.Entry<String, Object> entry : data.entrySet())
        {
            result.put(entry.getKey(), desensitizeValue(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private static Object desensitizeValue(String field, Object value)
    {
        if (!(value instanceof String text) || text.isEmpty())
        {
            return value;
        }
        String key = field == null ? "" : field.toLowerCase();
        if (key.contains("idcard") || key.contains("identity"))
        {
            return maskIdCard(text);
        }
        if (key.contains("name"))
        {
            return maskName(text);
        }
        if (key.contains("diagnosis"))
        {
            return maskDiagnosis(text);
        }
        return value;
    }

    private static String maskName(String value)
    {
        if (value.length() == 1)
        {
            return "*";
        }
        if (value.length() == 2)
        {
            return value.charAt(0) + "*";
        }
        return value.charAt(0) + repeat('*', value.length() - 2) + value.charAt(value.length() - 1);
    }

    private static String maskIdCard(String value)
    {
        if (value.length() <= 7)
        {
            return repeat('*', value.length());
        }
        return value.substring(0, 3) + repeat('*', value.length() - 7) + value.substring(value.length() - 4);
    }

    private static String maskDiagnosis(String value)
    {
        if (value.length() <= 2)
        {
            return repeat('*', value.length());
        }
        return value.substring(0, 2) + "***";
    }

    private static String repeat(char ch, int count)
    {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++)
        {
            builder.append(ch);
        }
        return builder.toString();
    }
}
