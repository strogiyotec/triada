package io.triada.models.hash;

import io.triada.models.hash.TxnHash;
import lombok.AllArgsConstructor;

import java.math.BigInteger;

/**
 * Constant Hash and Nonce of Transaction
 */
@AllArgsConstructor
public final class ConstTxnHash implements TxnHash {

    private final String hash;

    private final BigInteger nonce;

    @Override
    public String hash() {
        return this.hash;
    }

    @Override
    public BigInteger nonce() {
        return this.nonce;
    }
}
