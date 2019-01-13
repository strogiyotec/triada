package io.triada.models.wallet;

import com.google.common.hash.Hashing;
import io.triada.models.amount.Amount;
import io.triada.models.amount.TxnAmount;
import io.triada.models.head.Head;
import io.triada.models.head.HeadOfWallet;
import io.triada.models.key.Key;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTxnFromText;
import io.triada.models.transactions.SignedTxns;
import io.triada.models.transactions.SignedTxnsFromFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class TriadaWallet implements Wallet {

    /**
     * The extension of the wallet file
     */
    private static final String EXT = ".trd";

    private final File file;

    private final String fileContent;

    private final SignedTxns<SignedTxnFromText> txns;

    private final Head head;

    public TriadaWallet(final File file) throws IOException {
        this.fileContent = FileUtils.readFileToString(file, UTF_8);
        this.txns = new SignedTxnsFromFile(this.fileContent, file);
        this.head = new HeadOfWallet(this.fileContent);
        this.file = file;


    }

    @Override
    public Key walletKey() {
        return null;
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

    @Override
    public String asText() {
        return this.head.id();
    }
}
