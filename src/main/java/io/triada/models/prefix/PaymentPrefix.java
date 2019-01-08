package io.triada.models.prefix;

import io.triada.models.id.Id;
import io.triada.models.id.WalletId;

public final class PaymentPrefix implements Prefix {

    private final Id<Long> walletId;

    public PaymentPrefix(final WalletId walletId) {
        this.walletId = walletId;
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
