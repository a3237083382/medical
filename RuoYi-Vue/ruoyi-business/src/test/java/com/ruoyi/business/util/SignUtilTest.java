package com.ruoyi.business.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SignUtilTest
{
    @Test
    void verifyAcceptsSignatureGeneratedWithSameInputs()
    {
        String sign = SignUtil.sign("1700000000000", "nonce-1", "{\"name\":\"test\"}", "secret");

        assertTrue(SignUtil.verify("1700000000000", "nonce-1", "{\"name\":\"test\"}", "secret", sign));
    }

    @Test
    void verifyRejectsChangedBody()
    {
        String sign = SignUtil.sign("1700000000000", "nonce-1", "{\"name\":\"test\"}", "secret");

        assertFalse(SignUtil.verify("1700000000000", "nonce-1", "{\"name\":\"changed\"}", "secret", sign));
    }
}
