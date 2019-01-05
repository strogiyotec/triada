package io.triada.models;

import lombok.experimental.Delegate;

import java.util.Random;

/**
 * Generate random value in inclusive range
 */
public final class RandomNumber extends Number {

    private static final Random R = new Random();

    /**
     * Value
     */
    @Delegate
    private final Integer value;

    /**
     * @param min min value
     * @param max max value
     */
    public RandomNumber(final int min, int max) {
        max++;//inclusive
        this.value = RandomNumber.R.nextInt(max) % (max - min + 1) + min;
    }
}
