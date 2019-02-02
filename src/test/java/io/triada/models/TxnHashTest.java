package io.triada.models;

import io.triada.models.hash.BigIntegerHash;
import org.junit.Assert;
import org.junit.Test;

public final class TxnHashTest extends Assert {

    @Test
    public void testHash() {
        final BigIntegerHash suffix1 = new BigIntegerHash("Hello my friend", 3);
        final BigIntegerHash suffix2 = new BigIntegerHash("Hello my friend, how are you?", 4);

        assertTrue(suffix1.hash().endsWith("000"));
        assertTrue(suffix2.hash().endsWith("0000"));
    }
}
