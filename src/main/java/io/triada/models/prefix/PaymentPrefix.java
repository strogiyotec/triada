package io.triada.models.prefix;

import io.triada.models.wallet.Wallet;

public final class PaymentPrefix implements Prefix {

    private final Wallet wallet;

    public PaymentPrefix(final Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public String create(final int length) {
        if (length < 8 || length > 32) {
            throw new IllegalArgumentException(
                    String.format(
                            "Prefix length should be between 8 and 32 actual : %d",
                            length
                    )
            );
        }
        return null;
    }
}
