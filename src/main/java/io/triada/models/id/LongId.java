package io.triada.models.id;

import io.triada.models.random.RandomBigInteger;
import io.triada.text.HexNumber;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * Wallet id
 */
@EqualsAndHashCode(of = "id")
public final class LongId implements Id<Long> {

    /**
     * Id pattern
     */
    private static final Pattern ID_PTN = Pattern.compile("^[0-9a-fA-F]{16}$");

    /**
     * Root wallet
     */
    public static final LongId ROOT = new LongId("0000000000000000");

    /**
     * Id
     */
    private final Long id;

    public LongId(final String id) {
        validate(id);
        this.id = new BigInteger(id, 16).longValue();
    }

    public LongId(final Long id) {
        this.id = id;
    }

    /**
     * Create random id
     */
    public LongId() {
        final RandomBigInteger randomNumber =
                new RandomBigInteger(
                        BigInteger.valueOf(2).pow(32),
                        BigInteger.valueOf(2).pow(50).subtract(BigInteger.ONE)
                );
        this.id = randomNumber.get().longValueExact();
    }

    /**
     * @return Id
     */
    @Override
    public Long id() {
        return this.id;
    }

    /**
     * Validate given id
     *
     * @param id to validate
     */
    private static void validate(final String id) {
        if (!ID_PTN.matcher(id).find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid wallet id %s",
                            id
                    )
            );
        }
    }

    @Override
    public String toString() {
        return this.asText();
    }

    /**
     * @return 16 chars length representation of id in hex
     */
    @Override
    public String asText() {
        return new HexNumber(16, this.id).asText();
    }
}
