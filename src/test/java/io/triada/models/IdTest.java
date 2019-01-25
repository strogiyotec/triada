package io.triada.models;

import io.triada.models.id.LongId;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public final class IdTest extends Assert {

    @Test
    public void testParseId() {
        assertThat(
                new LongId("0000000000012345").id(),
                is(74565L)
        );
    }

    @Test
    public void testHexValue() {
        assertThat(
                new LongId("0000000000012345").toString(),
                is("12345")
        );
    }

    @Test
    public void testHexValueLength() {
        assertThat(
                new LongId().toString().length(),
                is(16)
        );
    }

    @Test
    public void testRandomWalletId() {
        assertTrue(new LongId().id()>4294967296L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonValidId() {
        final Long id = new LongId("000000000001234").id();
    }
}
