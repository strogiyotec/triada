package io.triada.models.hash;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

/**
 * Hash Envelope
 */
@AllArgsConstructor
public abstract class HashEnvelope implements Hash {

    private final Hash origin;

    HashEnvelope(final Supplier<Hash> hash) {
        this.origin = hash.get();
    }

    @Override
    public final String hash() {
        return this.origin.hash();
    }

    @Override
    public final String nonce() {
        return this.origin.nonce();
    }
}
