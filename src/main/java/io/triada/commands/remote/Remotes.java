package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.node.NodeData;
import io.triada.node.farm.Farm;
import org.jooq.lambda.fi.util.function.CheckedConsumer;

import java.util.Collections;
import java.util.List;

public interface Remotes {

    List<NodeData> all() throws Exception;

    void clean() throws Exception;

    void error(HostAndPort hostAndPort) throws Exception;

    void unError(HostAndPort hostAndPort) throws Exception;

    boolean exists(HostAndPort hostAndPort) throws Exception;

    void add(HostAndPort hostAndPort) throws Exception;

    void remove(HostAndPort hostAndPort) throws Exception;

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
