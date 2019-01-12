package io.triada.models.wallet;

import io.triada.models.head.Head;
import io.triada.models.head.HeadOfWallet;
import io.triada.models.key.Key;
import io.triada.models.transaction.SignedTxnFromText;
import io.triada.models.transactions.SignedTxns;
import io.triada.models.transactions.SignedTxnsFromFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class TriadaWallet implements Wallet {

    /**
     * The extension of the wallet file
     */
    private static final String EXT = ".trd";

    private final File file;

    private final SignedTxns<SignedTxnFromText> txns;

    private final Head head;

    public TriadaWallet(final File file) throws IOException {
        final String context = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        this.txns = new SignedTxnsFromFile(context);
        this.head = new HeadOfWallet(context);
        this.file = file;


    }

    @Override
    public Key walletKey() {
        return null;
    }

    @Override
    public String mnemo() {
        return null;
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
