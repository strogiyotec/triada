package io.triada.models.amount;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Wallet amount
 * TODO::wrong calculate with double values
 */
@AllArgsConstructor
@Slf4j
public final class TxnAmount implements Amount<Long> {

    /**
     * Max amount of tridz
     */
    private static final long MAX_TRIDZ = (long) Math.pow(2, 63);

    /**
     * Zero
     */
    public static final TxnAmount ZERO = new TxnAmount(0L);

    /**
     * How many tridz in one TRIADA: 2^FRACTION
     */
    private static final int FRACTION = 32;

    /**
     * Amount of tridz
     */
    private final Long tridz;

    /**
     * @param triads Amount of triads
     */
    public TxnAmount(final BigDecimal triads) {
        this.tridz = new BigDecimal(2).pow(FRACTION).multiply(triads).longValue();
    }

    /**
     * @param tridz Amount of tridz in hex
     */
    public TxnAmount(final String tridz) {
        this.tridz = new BigInteger(tridz, 16).longValue();
    }

    /**
     * @return Value of tridz
     */
    @Override
    public Long value() {
        return this.tridz;
    }

    @Override
    public TxnAmount substract(final Long value) {
        return new TxnAmount(this.tridz - value);
    }

    @Override
    public TxnAmount add(final Long value) {
        return new TxnAmount(this.tridz + value);
    }

    @Override
    public TxnAmount mpy(final Long value) {
        return new TxnAmount(this.tridz * value);
    }

    @Override
    public TxnAmount divide(final Long value) {
        return new TxnAmount(this.tridz / value);
    }

    @Override
    public boolean positive() {
        return this.tridz > 0;
    }

    @Override
    public boolean zero() {
        return this.tridz.equals(0L);
    }

    @Override
    public boolean bigger(final Long other) {
        return this.tridz > other;
    }

    @Override
    public boolean biggerOrEq(final Long other) {
        return this.bigger(other) || this.tridz.equals(other);
    }

    @Override
    public boolean less(final Long other) {
        return this.tridz < other;
    }

    @Override
    public boolean lessOrEq(final Long other) {
        return this.less(other) || this.tridz.equals(other);
    }

    /**
     * @param digits after dot
     * @return Amount of Triads from tridz
     */
    @Override
    public String asText(final int digits) {
        final BigDecimal divide = new BigDecimal(this.tridz).divide(new BigDecimal(2).pow(FRACTION), digits, RoundingMode.DOWN);
        return divide.toString();
    }

    @Override
    public String toString() {
        return this.asText(2) + "TRIADA";
    }
}
