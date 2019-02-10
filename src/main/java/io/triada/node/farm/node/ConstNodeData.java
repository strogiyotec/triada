package io.triada.node.farm.node;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.io.FileUtils.readFileToString;

/**
 * Simple data provider for node
 */
@AllArgsConstructor
public final class ConstNodeData implements NodeData {

    private final String host;

    private final int port;

    private final int errors;

    private final boolean master;

    private final int score;

    public static final List<NodeData> MASTERS = ConstNodeData.masters();

    public ConstNodeData(final HostAndPort hostAndPort) {
        this.host = hostAndPort.getHost();
        this.port = hostAndPort.getPort();
        this.errors = 0;
        this.master = false;
        this.score = 0;
    }

    public ConstNodeData(final String line) {
        final String[] parts = line.split(",");
        this.host = parts[0];
        this.port = Integer.parseInt(parts[1]);
        this.score = 0;
        this.errors = 0;
        this.master = false;
    }

    @Override
    public String host() {
        return this.host;
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public int errors() {
        return this.errors;
    }

    @Override
    public int score() {
        return this.score;
    }

    @Override
    public boolean master() {
        return this.master;
    }

    @Override
    public String asText() {
        return String.format(
                "%s,%d,%d,%d",
                this.host,
                this.port,
                this.score,
                this.errors
        );
    }

    /**
     * @return List of masters from master file
     */
    private static List<NodeData> masters() {
        try {
            final String[] lines = readFileToString(
                    new File(ConstNodeData.class.getClassLoader().getResource("/masters.txt").getFile()),
                    StandardCharsets.UTF_8
            ).split(System.lineSeparator());

            return Stream
                    .of(lines)
                    .map(ConstNodeData::new)
                    .collect(ImmutableList.toImmutableList());
        } catch (final IOException e) {
            throw new UncheckedIOException("Error loading masters list", e);
        }
    }
}
