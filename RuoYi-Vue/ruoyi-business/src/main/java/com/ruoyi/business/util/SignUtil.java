package com.ruoyi.business.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class SignUtil
{
    private static final String HMAC_SHA256 = "HmacSHA256";

    private SignUtil()
    {
    }

    public static String sign(String timestamp, String nonce, String body, String appSecret)
    {
        try
        {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(bytes(appSecret), HMAC_SHA256));
            return Base64.getEncoder().encodeToString(mac.doFinal(bytes(payload(timestamp, nonce, body))));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to generate signature", e);
        }
    }

    public static boolean verify(String timestamp, String nonce, String body, String appSecret, String sign)
    {
        if (sign == null)
        {
            return false;
        }
        return MessageDigest.isEqual(bytes(sign(timestamp, nonce, body, appSecret)), bytes(sign));
    }

    private static String payload(String timestamp, String nonce, String body)
    {
        return value(timestamp) + "\n" + value(nonce) + "\n" + value(body);
    }

    private static String value(String value)
    {
        return value == null ? "" : value;
    }

    private static byte[] bytes(String value)
    {
        return value(value).getBytes(StandardCharsets.UTF_8);
    }
}
