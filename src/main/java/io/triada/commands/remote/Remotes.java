package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.node.NodeData;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public interface Remotes extends Iterable<RemoteNode> {

    List<NodeData> all() throws Exception;

    void clean() throws Exception;

    List<NodeData> masters();

    boolean exists(HostAndPort hostAndPort) throws Exception;

    void add(HostAndPort hostAndPort) throws Exception;

    void remove(HostAndPort hostAndPort) throws Exception;

    boolean master(HostAndPort hostAndPort);

    final class Empty implements Remotes {

        @Override
        public List<NodeData> all() {
            return Collections.emptyList();
        }

        @Override
        public void clean() {

        }

        @Override
        public List<NodeData> masters() {
            return Collections.emptyList();
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
        public boolean master(final HostAndPort hostAndPort) {
            return false;
        }

        @Override
        public Iterator<RemoteNode> iterator() {
            return Collections.emptyIterator();
        }
    }


}
