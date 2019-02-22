package io.triada.models;

import io.triada.models.amount.TxnAmount;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;

public final class TxnAmountTest extends Assert {

    @Test
    public void testParsedTriads() {
        assertThat(
                new TxnAmount(new BigDecimal("14.95")).toString(),
                is("14.95TRIADA")
        );
    }

    @Test
    public void testPrintWithManyDigits() {
        final TxnAmount walletAmount = new TxnAmount(new BigDecimal("0.12345678"));
        assertEquals("0.123", walletAmount.asText(3));
        assertEquals("0.1234", walletAmount.asText(4));
        assertEquals("0.12345", walletAmount.asText(5));
        assertEquals("0.123456", walletAmount.asText(6));
    }

    @Test
    public void testCompareWithZero() {
        final TxnAmount walletAmount = new TxnAmount(new BigDecimal("0.000001"));
        assertTrue(!walletAmount.zero());
        assertTrue(walletAmount.positive());
        assertTrue(walletAmount != TxnAmount.ZERO);
    }

    @Test
    public void testParsedTridz() {
        final TxnAmount walletAmount = new TxnAmount(900_000_000L);
        assertThat(walletAmount.toString(), is("0.20TRIADA"));
    }

    @Test
    public void testAdd() {
        assertThat(new TxnAmount(new BigDecimal("39.99")).add(new TxnAmount(new BigDecimal("39.99")).value()).asText(2), is("79.98"));
    }

    // TODO: 2/22/19 result is not correct now it's 0.02 need to fix
    @Test
    public void testSub() {
        assertThat(new TxnAmount(new BigDecimal("39.99")).substract(new TxnAmount(new BigDecimal("39.98")).value()).asText(2), is("0.01"));
    }

}
