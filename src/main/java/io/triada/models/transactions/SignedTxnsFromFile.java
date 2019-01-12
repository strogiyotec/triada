package io.triada.models.transactions;

import io.triada.models.transaction.SignedTxnFromText;

import java.io.File;
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

    public SignedTxnsFromFile(final File file) throws IOException {
        this(
                Stream.of(
                        readFileToString(file, UTF_8)
                                .split(System.getProperty("line.separator"))
                )
        );
    }

    public SignedTxnsFromFile(final String fileContent) {
        this(
                Stream.of(fileContent.split(System.getProperty("line.separator")))
        );
    }

    private SignedTxnsFromFile(final Stream<String> stream) {
        this.txns = stream
                .map(SignedTxnFromText::new)
                .skip(4)//because of Head
                .collect(Collectors.toList());
    }

    @Override
    public List<SignedTxnFromText> txns() {
        return this.txns;
    }

    @Override
    public String asText() {
       return this.txns.stream().map(SignedTxnFromText::asText).collect(Collectors.joining("\n"));
    }
}
