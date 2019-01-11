package io.triada.models.transaction;

import java.util.Iterator;

public final class CachedTransactions implements Iterable<SignedTxnFromText> {

    private final Iterator<SignedTxnFromText> origin;

    public CachedTransactions(final Iterator<SignedTxnFromText> origin) {
        this.origin = origin;
    }

    @Override
    public Iterator<SignedTxnFromText> iterator() {
        return this.origin;
    }
}
