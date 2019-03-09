package io.triada.models.wallet;

import io.triada.models.amount.Amount;
import io.triada.models.amount.TxnAmount;
import io.triada.models.head.Head;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.transaction.SignedTransaction;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

@AllArgsConstructor
abstract class WalletEnvelope implements Wallet {
    /**
     * Origin
     */
    private final Wallet origin;

    public WalletEnvelope(final Supplier<Wallet> origin) {
        this.origin = origin.get();
    }

    @Override
    public final long age() {
        return this.origin.age();
    }

    @Override
    public final File file() {
        return this.origin.file();
    }

    @Override
    public final Head head() {
        return this.origin.head();
    }

    @Override
    public final Amount<Long> balance() {
        return this.origin.balance();
    }

    @Override
    public final boolean exists(final int id, final LongId bnf) {
        return this.origin.exists(id, bnf);
    }

    @Override
    public final boolean prefix(final String prefix) {
        return this.origin.prefix(prefix);
    }

    @Override
    public final Wallet add(final SignedTransaction transaction) throws Exception {
        return this.origin.add(transaction);
    }

    @Override
    public final String mnemo() throws Exception {
        return this.origin.mnemo();
    }

    @Override
    public final Wallet substract(final TxnAmount amount, final String prefix, final LongId id, final RsaKey pvt, final String details, final Date date) throws Exception {
        return this.origin.substract(amount, prefix, id, pvt, details, date);
    }

    @Override
    public final List<SignedTransaction> transactions() {
        return this.origin.transactions();
    }

    @Override
    public final String asText() {
        return this.origin.asText();
    }
}
