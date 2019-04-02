package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.node.NodeData;
import io.triada.node.farm.Farm;
import org.jooq.lambda.fi.util.function.CheckedConsumer;

import java.util.Collections;
import java.util.List;

public interface Remotes {

    /**
     * @return List of remote nodes
     * @throws Exception if failed
     */
    List<NodeData> all() throws Exception;

    /**
     * Clean all remotes
     *
     * @throws Exception if failed
     */
    void clean() throws Exception;

    /**
     * Increase amount of errors for given host and port
     *
     * @param hostAndPort HostAndPort of node
     * @throws Exception if failed
     */
    void error(HostAndPort hostAndPort) throws Exception;

    /**
     * Decrease amount of errors for given host and port
     *
     * @param hostAndPort HostAndPort of node
     * @throws Exception if failed
     */
    void unError(HostAndPort hostAndPort) throws Exception;

    /**
     * @param hostAndPort HostAndPort
     * @return True if node with given host and port exists
     * @throws Exception failed
     */
    boolean exists(HostAndPort hostAndPort) throws Exception;

    /**
     * Add new node with given host and port
     *
     * @param hostAndPort HostAndPort
     * @throws Exception if failed
     */
    void add(HostAndPort hostAndPort) throws Exception;

    /**
     * Remove node with given host and port
     *
     * @param hostAndPort HostAndPort
     * @throws Exception if failed
     */
    void remove(HostAndPort hostAndPort) throws Exception;

    /**
     * Accept given consumer to all nodes in file
     * If exception was thrown ,increment error for remote
     *
     * @param consumer Consumer
     * @param farm     Farm
     * @throws Exception if failed
     */
    void modify(CheckedConsumer<RemoteNode> consumer, Farm farm) throws Exception;

    default void modify(CheckedConsumer<RemoteNode> consumer) throws Exception {
        this.modify(consumer, Farm.EMPTY);
    }

    default void remove(String host, int port) throws Exception {
        this.remove(HostAndPort.fromParts(host, port));
    }

    default void add(String host, int port) throws Exception {
        this.add(HostAndPort.fromParts(host, port));
    }

    default boolean exists(String host, int port) throws Exception {
        return this.exists(HostAndPort.fromParts(host, port));
    }

    final class Empty implements Remotes {

        @Override
        public List<NodeData> all() {
            return Collections.emptyList();
        }

        @Override
        public void clean() {

        }

        @Override
        public void error(final HostAndPort hostAndPort) throws Exception {

        }

        @Override
        public void unError(final HostAndPort hostAndPort) throws Exception {

        }

        @Override
        public boolean exists(final HostAndPort hostAndPort) {
            return false;
        }

        @Override
        public void add(final HostAndPort hostAndPort) {
            throw new UnsupportedOperationException("Can't add to empty nodes");
        }

        @Override
        public void remove(final HostAndPort hostAndPort) {
            throw new UnsupportedOperationException("Can't remove from empty nodes");
        }

        @Override
        public void modify(final CheckedConsumer<RemoteNode> consumer, final Farm farm) {

        }
    }


}
