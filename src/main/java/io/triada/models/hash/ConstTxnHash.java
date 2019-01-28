package io.triada.models.hash;

import lombok.AllArgsConstructor;

/**
 * Constant Hash and Nonce of Transaction
 */
@AllArgsConstructor
public final class ConstTxnHash implements Hash {

    private final String hash;

    private final String nonce;

    @Override
    public String hash() {
        return this.hash;
    }

    @Override
    public String nonce() {
        return this.nonce;
    }
}
