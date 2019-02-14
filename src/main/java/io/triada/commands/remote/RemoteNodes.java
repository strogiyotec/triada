package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.node.farm.node.ConstNodeData;
import io.triada.node.farm.node.NodeData;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;

public final class RemoteNodes implements Remotes {

    /**
     * Default port
     */
    public static final int PORT = 4096;

    /**
     * At what amount of errors we delete remote automatically
     */
    public static final int TOLERANCE = 8;

    /**
     * Default number of nodes to fetch
     */
    public static final int MAX_NODES = 16;

    private final File file;

    private final String network;

    private final int timeout;

    public RemoteNodes(final File file, final String network, final int timeout) {
        this.file = file;
        this.network = network;
        this.timeout = timeout;
    }

    public RemoteNodes(final File file) {
        this.file = file;
        this.network = "test";
        this.timeout = 60;
    }

    @Override
    public List<NodeData> all() throws Exception {
        final List<NodeData> list = this.load();
        final int maxScore = list.stream().max(Comparator.comparingInt(NodeData::score)).map(NodeData::score).filter(score -> score > 0).orElse(1);
        final int maxErrors = list.stream().max(Comparator.comparingInt(NodeData::errors)).map(NodeData::errors).filter(score -> score > 0).orElse(1);

        return list.stream()
                .sorted((o1, o2) -> sortValue(o2, maxErrors, maxScore) - sortValue(o1, maxErrors, maxScore))
                .collect(toList());
    }

    @Override
    public void clean() throws Exception {
        try (final FileWriter writer = new FileWriter(this.file, false)) {
            writer.write("");
        }
    }

    @Override
    public List<NodeData> masters() {
        return ConstNodeData.MASTERS;
    }

    @Override
    public boolean exists(final HostAndPort hostAndPort) throws Exception {
        return this
                .load()
                .stream()
                .anyMatch(node -> HostAndPort.fromParts(node.host(), node.port()).equals(hostAndPort));
    }

    @Override
    public void add(final HostAndPort hostAndPort) throws Exception {
        FileUtils.write(
                this.file,
                new ConstNodeData(hostAndPort).asText(),
                StandardCharsets.UTF_8,
                true
        );
    }

    /**
     * Rewrite given file without given {@link HostAndPort}
     *
     * @param hostAndPort to remove
     * @throws Exception if failed
     */
    @Override
    public void remove(final HostAndPort hostAndPort) throws Exception {
        final List<NodeData> nodes = this.load();
        try (final FileWriter writer = new FileWriter(this.file, false)) {
            for (final NodeData node : nodes) {
                if (!HostAndPort.fromParts(node.host(), node.port()).equals(hostAndPort)) {
                    writer.append(
                            String.format(
                                    "%s%s",
                                    node.asText(),
                                    System.lineSeparator()
                            )
                    );
                }
            }
        }
    }

    @Override
    public boolean master(final HostAndPort hostAndPort) {
        return ConstNodeData.MASTERS.stream().anyMatch(node -> HostAndPort.fromParts(node.host(), node.port()).equals(hostAndPort));
    }

    @Override
    public Iterator<NodeData> iterator() {
        try {
            return this.load().iterator();
        } catch (Exception e) {
            throw new IllegalStateException("Error getting iterator", e);
        }
    }

    /**
     * Load NodeData list from file
     * NodeData are provided in csv format with ',' as separator
     *
     * @return List of Node Data
     * @throws Exception if failed
     */
    private List<NodeData> load() throws Exception {
        final List<NodeData> rows = new ArrayList<>(16);
        final String content = FileUtils.readFileToString(
                this.file,
                StandardCharsets.UTF_8
        );

        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        } else {
            for (final String line : content.split(System.lineSeparator())) {
                final String[] row = line.split(",");
                final HostAndPort hostAndPort = HostAndPort.fromParts(row[0], Integer.parseInt(row[1]));
                rows.add(
                        new ConstNodeData(
                                hostAndPort.getHost(),
                                hostAndPort.getPort(),
                                Integer.parseInt(row[3]),
                                this.master(hostAndPort),
                                Integer.parseInt(row[2])
                        )
                );
            }
        }
        return rows;
    }

    private static int sortValue(final NodeData row, final int maxErrors, final int maxScore) {
        return (1 - row.errors() / maxErrors) * 5 + (row.score() / maxScore);
    }
}
