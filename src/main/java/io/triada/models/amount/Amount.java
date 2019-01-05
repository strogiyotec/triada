package io.triada.models.amount;

/**
 * Represent actions with some amount
 */
public interface Amount<T extends Number> {

    T value();

    Amount substract(T value);

    Amount add(T value);

    Amount mpy(T value);

    Amount divide(T value);

    boolean positive();

    boolean zero();

    boolean bigger(T other);

    boolean biggerOrEq(T other);

    boolean less(T other);

    boolean lessOrEq(T other);

    String asText(int digits);
}
