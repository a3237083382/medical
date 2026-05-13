package com.ruoyi.business.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
            String payload = timestamp + "\n" + nonce + "\n" + (body == null ? "" : body);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return toHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("sign failed", e);
        }
    }

    public static boolean verify(String timestamp, String nonce, String body, String appSecret, String sign)
    {
        if (isBlank(timestamp) || isBlank(nonce) || isBlank(appSecret) || isBlank(sign))
        {
            return false;
        }
        String expected = sign(timestamp, nonce, body, appSecret);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), sign.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes)
    {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
        {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
