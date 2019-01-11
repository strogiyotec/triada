package io.triada.models.head;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public final class HeadOfWallet implements Head {

    private final Callable<List<String>> head;

    public HeadOfWallet(final File file) {
        this.head = () -> {
            final String[] lines = FileUtils.readFileToString(file, StandardCharsets.UTF_8).split(System.lineSeparator());
            final int length = lines.length;
            if (length != 4) {
                throw new IllegalArgumentException(
                        String.format(
                                "File %s must contains 4 lines but has only %d",
                                file.getName(),
                                length
                        )
                );
            }
            return Arrays.asList(lines);
        };
    }

    @Override
    public List<String> head() throws Exception {
        return this.head.call();
    }
}
