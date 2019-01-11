package io.triada.models.transaction;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;

public final class TriadaTxns implements Iterable<SignedTxnFromText> {

    private final Iterator<SignedTxnFromText> iterator;

    public TriadaTxns(final File file) throws IOException {
        this.iterator = Stream.of(readFileToString(file, UTF_8)
                .split(System.getProperty("line.separator")))
                .map(SignedTxnFromText::new)
                .iterator();
        ;
    }

    @Override
    public Iterator<SignedTxnFromText> iterator() {
        return this.iterator;
    }
}
