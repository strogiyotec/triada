package io.triada.models.wallet;

import com.google.common.collect.ImmutableList;
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
import io.triada.models.transaction.SignedTxnFromText;
import io.triada.models.transaction.ValidatedTxn;
import io.triada.models.transactions.SignedTxns;
import io.triada.models.transactions.SignedTxnsFromFile;
import io.triada.text.NextTxnId;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

// TODO: 2/22/19 Ad lazy wallet on non existing file

/**
 * Main wallet of triada network
 */
public final class TriadaWallet implements Wallet {

    /**
     * The extension of the wallet file
     */
    public static final String EXT = ".trd";

    /**
     * File with txns
     */
    private final File file;

    /**
     * txns from file
     */
    private final SignedTxns txns;

    /**
     * Head from file
     */
    private final Head head;

    public TriadaWallet(final File file) throws IOException {
        final String fileContent = FileUtils.readFileToString(file, UTF_8);
        this.txns = new SignedTxnsFromFile(fileContent, file);
        this.head = new HeadOfWallet(fileContent);
        this.file = file;
    }

    @Override
    public long age() {
        final List<SignedTransaction> txns = this.txns.txns();
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
        return this.head;
    }

    @Override
    public Amount<Long> balance() {
        final long balance =
                this.txns.txns()
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
        return this.head.key().contains(prefix);
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
        return new TriadaWallet(this.file);
    }

    @Override
    public String mnemo() throws Exception {
        return String.join(
                ",",
                this.head.id(),
                this.balance().asText(4),
                String.valueOf(this.txns.txns().size()) + "t",
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
                ).signed(new LongId(this.head.id()), pvt)
        );
    }

    @Override
    public List<SignedTransaction> transactions() {
        return ImmutableList.copyOf(this.txns.txns());
    }


    @Override
    public String asText() {
        return this.head.id();
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
        return Objects.equals(this.head.id(), other.head().id());
    }

}
