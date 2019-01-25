package io.triada.models;

import io.triada.models.random.RandomBigInteger;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public final class TestRandomBigInt extends Assert {

    @Test
    public void generateHugeInt() {
        final RandomBigInteger randomBigInteger = new RandomBigInteger(
                BigInteger.valueOf(2).pow(32),
                BigInteger.valueOf(2).pow(64).subtract(BigInteger.ONE)
        );
        assertTrue(randomBigInteger.get().toString().length() == 20);
    }
}
