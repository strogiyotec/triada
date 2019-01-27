package io.triada.models;

import io.triada.models.hash.CalculatedHash;
import org.junit.Assert;
import org.junit.Test;

public final class TxnHashTest extends Assert{

    @Test
    public void testHash(){
        final CalculatedHash suffix1 = new CalculatedHash("Hello my friend", 3);
        final CalculatedHash suffix2 = new CalculatedHash("Hello my friend, how are you?", 4);

        assertTrue(suffix1.hash().endsWith("000"));
        assertTrue(suffix2.hash().endsWith("0000"));
    }
}
