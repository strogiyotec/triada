package io.triada.models;

import io.triada.models.id.WalletId;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public final class WalletIdTest extends Assert {

    @Test
    public void testParseId() {
        assertThat(
                new WalletId("0000000000012345").id(),
                is(74565L)
        );
    }

    @Test
    public void testHexValue() {
        assertThat(
                new WalletId("0000000000012345").toString(),
                is("12345")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonValidId() {
        final Long id = new WalletId("000000000001234").id();
    }
}
