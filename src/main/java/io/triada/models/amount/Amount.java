package io.triada.models.amount;

/**
 * Represent actions with some amount
 */
public interface Amount<T extends Number> {

    T value();

    Amount<T> substract(T value);

    Amount<T> add(T value);

    Amount<T> mpy(T value);

    Amount<T> divide(T value);

    boolean positive();

    boolean zero();

    boolean bigger(T other);

    boolean negative();

    boolean biggerOrEq(T other);

    boolean less(T other);

    boolean lessOrEq(T other);

    String asText(int digits);
}
