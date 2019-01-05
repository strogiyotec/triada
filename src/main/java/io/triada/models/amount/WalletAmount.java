package io.triada.models.amount;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Wallet amount
 */
@AllArgsConstructor
@Slf4j
public final class WalletAmount implements Amount<Long> {

    /**
     * Max aount of tridz
     */
    private static final long MAX_TRIDZ = (long) Math.pow(2, 63);

    /**
     * Zero
     */
    public static final WalletAmount ZERO = new WalletAmount(0L);

    /**
     * How many tridz in one TRIADA: 2^FRACTION
     */
    private static final int FRACTION = 32;

    /**
     * Amount of tridz
     */
    private final Long tridz;

    public WalletAmount(final BigDecimal triads) {
        this.tridz = new BigDecimal(2).pow(FRACTION).multiply(triads).longValue();
    }

    @Override
    public Long value() {
        return this.tridz;
    }

    @Override
    public Amount substract(final Long value) {
        return new WalletAmount(this.tridz - value);
    }

    @Override
    public Amount add(final Long value) {
        return new WalletAmount(this.tridz + value);
    }

    @Override
    public Amount mpy(final Long value) {
        log.info("ds", "ds");
        return new WalletAmount(this.tridz * value);
    }

    @Override
    public Amount divide(final Long value) {
        return new WalletAmount(this.tridz / value);
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
