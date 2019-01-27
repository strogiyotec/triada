package io.triada.models.hash;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public abstract class HashEnvelope implements TxnHash {

    private final TxnHash origin;

    HashEnvelope(final Supplier<TxnHash> hash) {
        this.origin = hash.get();
    }

    @Override
    public final String hash() {
        return this.origin.hash();
    }

    @Override
    public String nonce() {
        return this.origin.nonce();
    }
}
