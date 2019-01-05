package io.triada.models;

import io.triada.models.amount.WalletAmount;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;

public final class WalletAmountTest extends Assert {

    @Test
    public void testParsedTriads() {
        assertThat(
                new WalletAmount(new BigDecimal("14.95")).toString(),
                is("14.94TRIADA")
        );
    }

    @Test
    public void testPrintWithManyDigits(){
        final WalletAmount walletAmount = new WalletAmount(new BigDecimal("0.12345678"));
        assertEquals("0.123", walletAmount.asText(3));
        assertEquals("0.1234", walletAmount.asText(4));
        assertEquals("0.12345", walletAmount.asText(5));
        assertEquals("0.123456", walletAmount.asText(6));
    }

    @Test
    public void testCompareWithZero(){
        final WalletAmount walletAmount = new WalletAmount(new BigDecimal("0.000001"));
        assertTrue(!walletAmount.zero());
        assertTrue(walletAmount.positive());
        assertTrue(walletAmount != WalletAmount.ZERO);
    }

    @Test
    public void testParsedTridz(){
        final WalletAmount walletAmount = new WalletAmount(900_000_000L);
        assertThat(walletAmount.toString(),is("0.20TRIADA"));
    }

}
