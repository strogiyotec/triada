package io.triada.models.prefix;

import io.triada.models.key.RsaKey;

import java.util.Random;

public final class PaymentPrefix implements Prefix {

    private final RsaKey publicKey;

    public PaymentPrefix(final RsaKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String create(final int length) {
        final String asString = this.publicKey.asPublic();
        if (length < 8 || length > 32) {
            throw new IllegalArgumentException(
                    String.format(
                            "Prefix length should be between 8 and 32 actual : %d",
                            length
                    )
            );
        }
        final int start = new Random().nextInt(asString.length() - length);
        return asString.substring(start, start + length);
    }
}
