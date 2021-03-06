package io.triada.models.wallet;

import com.google.common.hash.Hashing;
import io.triada.dates.DateConverters;
import io.triada.models.amount.Amount;
import io.triada.models.amount.TxnAmount;
import io.triada.models.head.Head;
import io.triada.models.head.HeadOfWallet;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.ValidatedTxn;
import io.triada.models.transactions.SignedTxnsFromFile;
import io.triada.text.NextTxnId;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * This class doesn't store all txns in memory ,
 * Always read all txns from file
 * Doesn't store head in memory
 * Not Thread safe
 */
public final class EagerWallet implements Wallet {

    /**
     * File with txns
     */
    private final File file;

    public EagerWallet(final File file) {
        this.file = file;
    }

    @Override
    public long age() {
        final List<SignedTransaction> txns = this.transactions();
        if (txns.isEmpty()) {
            return 0L;
        } else {
            final ParsedTxnData minTxn =
                    txns.stream()
                            .map(ParsedTxnData::new)
                            .min(Comparator.comparing(ParsedTxnData::date))
                            .get();
            return Duration.between(DateConverters.toLocalDateTime(minTxn.date()), DateConverters.toLocalDateTime(new Date())).toHours();
        }
    }

    @Override
    public File file() {
        return this.file;
    }

    @Override
    public Head head() {
        try {
            return new HeadOfWallet(this.file);
        } catch (final IOException e) {
            throw new UncheckedIOException("Error reading head", e);
        }
    }

    @Override
    public Amount<Long> balance() {
        final long balance =
                this.transactions()
                        .stream()
                        .map(ParsedTxnData::new)
                        .map(ParsedTxnData::amount)
                        .mapToLong(TxnAmount::value)
                        .sum();

        return new TxnAmount(balance);
    }

    @Override
    public boolean exists(final int id, final LongId bnf) {
        return this.transactions()
                .stream()
                .map(ParsedTxnData::new)
                .anyMatch(p -> p.id() == id && p.bnf().equals(bnf));
    }

    @Override
    public boolean prefix(final String prefix) {
        return this.head().key().contains(prefix);
    }

    @Override
    public Wallet add(final SignedTransaction transaction) throws IOException {
        try (final FileWriter writer = new FileWriter(this.file, true)) {
            writer.append(
                    String.format(
                            "%s%s;%s",
                            System.lineSeparator(),
                            transaction.asText(),
                            transaction.signature()
                    )
            );
        }
        return this;
    }

    @Override
    public String mnemo() throws Exception {
        return String.join(
                ",",
                this.head().id(),
                this.balance().asText(4),
                String.valueOf(this.transactions().size()) + "t",
                Hashing.sha256().hashBytes(Files.readAllBytes(this.file.toPath())).toString().substring(0, 6)
        );
    }

    /**
     * Originally was implemented as invoice.split(@) instead of PREFIX + id
     */
    @Override
    public Wallet substract(
            final TxnAmount amount,
            final String prefix,
            final LongId id,
            final RsaKey pvt,
            final String details,
            final Date date
    ) throws Exception {
        return this.add(
                new ValidatedTxn(
                        NextTxnId.next(this.transactions()),
                        date,
                        amount.mpy(-1L),
                        prefix,
                        id,
                        details
                ).signed(new LongId(this.head().id()), pvt)
        );
    }

    @Override
    public List<SignedTransaction> transactions() {
        try {
            return new SignedTxnsFromFile(this.file).txns();
        } catch (final IOException e) {
            throw new UncheckedIOException("Can't read wallet file ", e);
        }
    }


    @Override
    public String asText() {
        return this.head().id();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final Wallet other = (Wallet) obj;
        return Objects.equals(this.head().id(), other.head().id());
    }

}
