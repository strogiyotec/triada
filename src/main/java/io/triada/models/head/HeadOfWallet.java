package io.triada.models.head;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class HeadOfWallet implements Head {

    private static final Pattern NETWORK_PTN = Pattern.compile("^[a-z]{4,16}$");

    private static final Pattern PROTOCOL_PTN = Pattern.compile("^[0-9]+$");

    private final List<String> head;

    public HeadOfWallet(final File file) throws IOException {
        this(
                FileUtils.readFileToString(file, StandardCharsets.UTF_8).split(System.lineSeparator())
        );
    }

    public HeadOfWallet(final String content) {
        this(
                content.split(System.lineSeparator())
        );
    }

    private HeadOfWallet(final String[] lines) {
        final int length = lines.length;
        if (length < 4) {
            throw new IllegalArgumentException(
                    String.format(
                            "Head must contains 4 lines but has only %d",
                            length
                    )
            );
        }
        this.head = Arrays.asList(lines);
    }

    @Override
    public String network() {
        final String network = this.head.get(0);
        if (!NETWORK_PTN.matcher(network).find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid network name %s Pattern : %s",
                            network,
                            NETWORK_PTN.pattern()
                    )
            );
        }
        return network;
    }

    @Override
    public String protocol() {
        final String protocol = this.head.get(1);
        if (!PROTOCOL_PTN.matcher(protocol).find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid protocol name %s Pattern : %s",
                            protocol,
                            PROTOCOL_PTN.pattern()
                    )
            );
        }
        return protocol;
    }

    @Override
    public String id() {
        return this.head.get(2);
    }

    @Override
    public String key() {
        final String key = this.head.get(3);
        if (!key.startsWith("MIICI")) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid key '%s' should start with MIICI",
                            key
                    )
            );
        }
        return key;
    }
}