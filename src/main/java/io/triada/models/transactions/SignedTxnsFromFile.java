package io.triada.models.transactions;

import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.SignedTxnFromText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;

/**
 * Get list of {@link io.triada.models.transaction.SignedTransaction} from File
 */
public final class SignedTxnsFromFile implements SignedTxns {

    private final List<SignedTransaction> txns;

    private final File file;

    public SignedTxnsFromFile(final File file) throws IOException {
        this(
                Files.lines(file.toPath()),
                file
        );
    }

    public SignedTxnsFromFile(final String fileContent, final File file) {
        this(
                Stream.of(fileContent.split(System.getProperty("line.separator"))),
                file
        );
    }

    private SignedTxnsFromFile(final Stream<String> stream, final File file) {
        this.txns = stream
                .skip(5)//because of Head + EMPTY LINE
                .map(SignedTxnFromText::new)
                .collect(Collectors.toList());
        this.file = file;
    }

    @Override
    public List<SignedTransaction> txns() {
        return this.txns;
    }

    @Override
    public String asText() {
        return this.txns.stream().map(SignedTransaction::asText).collect(Collectors.joining("\n"));
    }

    @Override
    public SignedTxns add(final SignedTransaction txn) throws Exception {
        appendTxnToFile(this.file, txn);
        return new SignedTxnsFromFile(this.file);
    }

    /**
     * Append new Txn to given file
     *
     * @param file To save
     * @param txn  TO save
     * @throws IOException if failed
     */
    private static void appendTxnToFile(final File file, final SignedTransaction txn) throws IOException {
        final ParsedTxnData txnData = new ParsedTxnData(txn);
        try (final FileWriter writer = new FileWriter(file, true)) {
            writer.append(String.format(
                    "%s%s",
                    txnData.asText(),
                    System.lineSeparator()
            ));
        }
    }
}
