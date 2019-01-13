package io.triada.models.random;

import java.math.BigInteger;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Generate random value in inclusive range
 */
public final class RandomBigInteger implements Supplier<BigInteger> {

    private final static Random R = new Random();

    private final BigInteger origin;

    /**
     * @param min min value
     * @param max max value
     */
    public RandomBigInteger(BigInteger min, BigInteger max) {
        max = max.add(BigInteger.ONE);
        BigInteger range = max.subtract(min);
        final int length = range.bitLength();
        BigInteger result = new BigInteger(length, R);
        while (result.compareTo(range) >= 0) {
            result = new BigInteger(length, R);
        }
        this.origin = result.add(min);
    }

    @Override
    public BigInteger get() {
        return this.origin;
    }
}
