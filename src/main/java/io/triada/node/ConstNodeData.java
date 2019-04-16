package io.triada.node;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Simple data provider for node
 */
@AllArgsConstructor
@EqualsAndHashCode(of = {"host", "port"})
public final class ConstNodeData implements NodeData {

    /**
     * Host of node
     */
    private final String host;

    /**
     * Port of node
     */
    private final int port;

    /**
     * Amount of errors in node
     */
    private final int errors;

    /**
     * Score of node
     */
    private final int score;

    /**
     * List of predefined masters
     */
    public static final List<NodeData> MASTERS = ConstNodeData.masters();

    public ConstNodeData(final HostAndPort hostAndPort) {
        this.host = hostAndPort.getHost();
        this.port = hostAndPort.getPort();
        this.errors = 0;
        this.score = 0;
    }

    /**
     * Create node from text
     *
     * @param line Text
     */
    public ConstNodeData(final String line) {
        final String[] parts = line.split(",");
        this.host = parts[0];
        this.port = Integer.parseInt(parts[1]);
        this.score = 0;
        this.errors = 0;
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
        return ConstNodeData.MASTERS.stream().anyMatch(node -> HostAndPort.fromParts(node.host(), node.port()).equals(HostAndPort.fromParts(this.host, this.port)));
    }

    @Override
    public String asText(final String host, final int port, final int errors, final int score) {
        return String.format(
                "%s,%d,%d,%d\n",
                host,
                port,
                score,
                errors
        );
    }

    @Override
    public String asText() {
        return String.format(
                "%s,%d,%d,%d\n",
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
            return Files.lines(new File(ConstNodeData.class.getClassLoader().getResource("masters.txt").getFile()).toPath())
                    .filter(line -> !StringUtils.isEmpty(line))
                    .map(ConstNodeData::new)
                    .collect(ImmutableList.toImmutableList());
        } catch (final IOException e) {
            throw new UncheckedIOException("Error loading masters list", e);
        }
    }

}
