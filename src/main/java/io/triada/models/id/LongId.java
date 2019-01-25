package io.triada.models.id;

import io.triada.models.random.RandomBigInteger;
import io.triada.text.HexText;
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
    private static final Id<Long> ROOT = new LongId("0000000000000000");

    /**
     * Id
     */
    private final Long id;

    public LongId(final String id) {
        validate(id);
        this.id = new BigInteger(id, 16).longValue();
    }

    public LongId() {
        final RandomBigInteger randomNumber =
                new RandomBigInteger(
                        BigInteger.valueOf(2).pow(32),
                        BigInteger.valueOf(2).pow(50).subtract(BigInteger.ONE)
                );
        this.id = randomNumber.get().longValueExact();
    }


    @Override
    public Long id() {
        return this.id;
    }

    /**
     * @return 16 chars length representation of id in hex
     */
    @Override
    public String toString() {
        final StringBuilder hexValue = new StringBuilder(
                new HexText(id).toString()
        );
        while (hexValue.length() != 16) {
            hexValue.insert(0, '0');
        }
        return hexValue.toString();
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
}
