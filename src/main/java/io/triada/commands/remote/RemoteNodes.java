package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.node.ConstNodeData;
import io.triada.node.NodeData;
import io.triada.node.farm.Farm;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.fi.util.function.CheckedConsumer;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.net.HostAndPort.fromParts;
import static java.util.stream.Collectors.toList;

/**
 * All remotes
 */
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

    /**
     * File to store remotes
     */
    private final File file;

    /**
     * Network name
     */
    private final String network;

    /**
     * Wait period for all threads to terminate
     */
    private final int timeout;

    public RemoteNodes(
            final File file,
            final String network,
            final int timeout
    ) {
        this.file = file;
        this.network = network;
        this.timeout = timeout;
    }

    public RemoteNodes(final File file) {
        this.file = file;
        this.network = "test";
        this.timeout = 60;
    }

    /**
     * @return List of nodes from file
     * @throws Exception if failed
     */
    @Override
    public List<NodeData> all() throws Exception {
        final List<NodeData> list = this.load();
        final int maxScore = list.stream().max(Comparator.comparingInt(NodeData::score)).map(NodeData::score).filter(score -> score > 0).orElse(1);
        final int maxErrors = list.stream().max(Comparator.comparingInt(NodeData::errors)).map(NodeData::errors).filter(score -> score > 0).orElse(1);

        return list.stream()
                .sorted((o1, o2) -> sortValue(o2, maxErrors, maxScore) - sortValue(o1, maxErrors, maxScore))
                .collect(toList());
    }

    /**
     * Clean all remotes
     *
     * @throws Exception if failed
     */
    @Override
    public void clean() throws Exception {
        try (final FileWriter writer = new FileWriter(this.file, false)) {
            writer.write("");
        }
    }

    /**
     * Increment error value for node with given host and port
     *
     * @param hostAndPort HostAndPort of node
     * @throws Exception if failed
     */
    @Override
    public synchronized void error(final HostAndPort hostAndPort) throws Exception {
        final List<NodeData> load = this.load();
        try (final FileWriter writer = new FileWriter(this.file, false)) {
            for (final NodeData node : load) {
                if (!fromParts(node.host(), node.port()).equals(hostAndPort)) {
                    writer.append(node.asText());
                } else {
                    writer.append(node.asText(node.host(), node.port(), node.errors() + 1, node.score()));
                }
            }
        }
    }

    // TODO: 2/24/19 Need to implement
    @Override
    public synchronized void unError(final HostAndPort hostAndPort) throws Exception {

    }

    /**
     * @param hostAndPort HostAndPort
     * @return true if file contains node with given host and port
     * @throws Exception if failed
     */
    @Override
    public boolean exists(final HostAndPort hostAndPort) throws Exception {
        return this
                .load()
                .stream()
                .anyMatch(node -> fromParts(node.host(), node.port()).equals(hostAndPort));
    }

    /**
     * @param hostAndPort Host and port of node to add to file
     * @throws Exception if failed
     */
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
        final List<NodeData> load = this.load();
        try (final FileWriter writer = new FileWriter(this.file, false)) {
            for (final NodeData node : load) {
                if (!fromParts(node.host(), node.port()).equals(hostAndPort)) {
                    writer.append(node.asText());
                }
            }
        }
    }

    /**
     * Accept given consumer to all nodes in file
     * If exception was thrown ,increment error for remote
     *
     * @param consumer Consumer
     * @param farm     Farm
     * @throws Exception if failed
     */
    @Override
    public void modify(final CheckedConsumer<RemoteNode> consumer, final Farm farm) throws Exception {
        final ExecutorService service = Executors.newFixedThreadPool(4);
        final AtomicInteger idx = new AtomicInteger(0);
        for (final NodeData nodeData : this.load()) {
            service.submit(() -> {
                try {
                    final RemoteNode remoteNode =
                            new RemoteNode(
                                    farm,
                                    nodeData,
                                    this.network,
                                    idx.getAndIncrement()
                            );
                    consumer.accept(remoteNode);
                    if (remoteNode.touched()) {
                        this.unError(remoteNode.address());
                    }
                } catch (final Throwable exc) {
                    final int errors = nodeData.errors() + 1;
                    final HostAndPort hostAndPort = fromParts(nodeData.host(), nodeData.port());
                    if (errors > TOLERANCE) {
                        this.remove(hostAndPort);
                    } else {
                        this.error(hostAndPort);
                    }
                }
                return null;//because we need callable instead of Runnable
            });
        }
        service.shutdown();
        service.awaitTermination(this.timeout, TimeUnit.SECONDS);
    }

    /**
     * Load NodeData list from file
     * NodeData are provided in csv format with ',' as separator
     *
     * @return List of Node Data
     * @throws Exception if failed
     */
    private List<NodeData> load() throws Exception {
        return Files.lines(this.file.toPath())
                .map(line -> {
                    final String[] row = line.split(",");
                    final HostAndPort hostAndPort = fromParts(row[0], Integer.parseInt(row[1]));
                    return new ConstNodeData(
                            hostAndPort.getHost(),
                            hostAndPort.getPort(),
                            Integer.parseInt(row[3]),
                            Integer.parseInt(row[2])
                    );
                }).collect(toList());
    }

    private static int sortValue(final NodeData row, final int maxErrors, final int maxScore) {
        return (1 - row.errors() / maxErrors) * 5 + (row.score() / maxScore);
    }
}
