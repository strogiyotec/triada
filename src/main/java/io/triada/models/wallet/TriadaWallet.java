package io.triada.models.wallet;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import io.triada.models.amount.Amount;
import io.triada.models.amount.TxnAmount;
import io.triada.models.head.Head;
import io.triada.models.head.HeadOfWallet;
import io.triada.models.id.LongId;
import io.triada.models.key.Key;
import io.triada.models.key.RsaKey;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.SignedTxnFromText;
import io.triada.models.transaction.ValidatedTxn;
import io.triada.models.transactions.SignedTxns;
import io.triada.models.transactions.SignedTxnsFromFile;
import io.triada.text.HexText;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

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
     * Content of file with txns
     */
    private final String fileContent;

    /**
     * txns from file
     */
    private final SignedTxns<SignedTxnFromText> txns;

    /**
     * Head from file
     */
    private final Head head;

    public TriadaWallet(final File file) throws IOException {
        this.fileContent = FileUtils.readFileToString(file, UTF_8);
        this.txns = new SignedTxnsFromFile(this.fileContent, file);
        this.head = new HeadOfWallet(this.fileContent);
        this.file = file;


    }

    @Override
    public Key walletKey() {
        return new RsaKey(this.head.key());
    }

    @Override
    public String mnemo() {
        return String.join(
                "/",
                this.head.id(),
                this.balance().asText(4),
                String.valueOf(this.txns.txns().size()),
                Hashing.sha256().hashString(this.fileContent, UTF_8).toString(),
                String.valueOf(this.file.getTotalSpace())
        );
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
    public Wallet add(final SignedTransaction transaction) throws IOException {
        try (final FileWriter writer = new FileWriter(this.file, true)) {
            writer.append(
                    String.format(
                            "%s;%s%s",
                            transaction.origin().body(),
                            transaction.signature(),
                            System.lineSeparator()
                    )
            );
            return new TriadaWallet(this.file);
        }
    }

    @Override
    public Wallet substract(
            final TxnAmount amount,
            final String prefix,
            final LongId id,
            final RsaKey pvt,
            final String details
    ) throws Exception {
        final TxnAmount negative = amount.mpy(-1L);
        final String tid = this.maxTxnId();
        final ValidatedTxn validatedTxn = new ValidatedTxn(
                tid,
                new Date(),
                negative,
                prefix,
                id,
                details
        );
        final SignedTransaction signed = validatedTxn.signed(new LongId(this.head.id()), pvt);

        return this.add(signed);
    }

    @Override
    public List<SignedTransaction> transactions() {
        return ImmutableList.copyOf(this.txns.txns());
    }


    @Override
    public String asText() {
        return this.head.id();
    }

    /**
     * Calculate max txn id , if no txns return 0 in hex , otherwise
     * find last txn id increment it and return in hex
     *
     * @return Next txn id
     */
    private String maxTxnId() {
        final List<SignedTxnFromText> transactions = this.txns.txns();
        if (transactions.isEmpty()) {
            return Long.toHexString(1L);
        } else {
            return new HexText(
                    Long.parseLong(
                            new ParsedTxnData(
                                    transactions.get(transactions.size() - 1)
                            ).id()+1
                            , 16
                    )
            ).toString();
        }
    }

    /**
     * Validate extension of given file
     *
     * @param file to validate
     */
    private static void validate(final File file) {
        if (!file.getAbsolutePath().contains(EXT)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Wallet file must end with %s",
                            EXT
                    )
            );
        }
    }

}
