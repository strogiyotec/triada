package io.triada.models.transactions;

import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.SignedTxnFromText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;

/**
 * Get list of {@link io.triada.models.transaction.SignedTransaction} from File
 */
public final class SignedTxnsFromFile implements SignedTxns<SignedTxnFromText> {

    private final List<SignedTxnFromText> txns;

    private final File file;

    public SignedTxnsFromFile(final File file) throws IOException {
        this(
                Stream.of(readFileToString(file, UTF_8).split(System.getProperty("line.separator"))),
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
                .map(SignedTxnFromText::new)
                .skip(4)//because of Head
                .collect(Collectors.toList());
        this.file = file;
    }

    @Override
    public List<SignedTxnFromText> txns() {
        return this.txns;
    }

    @Override
    public String asText() {
        return this.txns.stream().map(SignedTxnFromText::asText).collect(Collectors.joining("\n"));
    }

    @Override
    public SignedTxns<SignedTxnFromText> add(final SignedTransaction txn) throws Exception {
        appendTxnToFile(this.file, txn);
        return new SignedTxnsFromFile(this.file);
    }

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
