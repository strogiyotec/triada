package io.triada.node.remote;

import com.google.common.net.HostAndPort;
import io.triada.commands.remote.RemoteNodes;
import io.triada.node.ConstNodeData;
import io.triada.node.NodeData;
import io.triada.node.farm.Farm;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.List;

import static com.google.common.net.HostAndPort.fromParts;
import static io.triada.commands.remote.RemoteNodes.PORT;

public final class TestRemotes extends Assert {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void testAddRemote() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        nodes.add(fromParts("127.0.0.1", PORT));
        nodes.add(fromParts("127.0.0.2", PORT));
        assertTrue(nodes.all().size() == 2);
    }

    @Test
    public void testFindMasters() throws Exception {
        assertTrue(new ConstNodeData(fromParts("127.0.0.1", 4096)).master());
    }

    @Test
    public void testIterateAndFails() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        for (int i = 0; i < 10; i++) {
            nodes.add(HostAndPort.fromParts("0.0.0.0", i));
        }
        nodes.modify(remoteNode -> {
            throw new Exception("For test");
        }, Farm.EMPTY);
        final List<NodeData> all = nodes.all();
        assertTrue(all.size() == 10);
        for (final NodeData nodeData : nodes.all()) {
            assertTrue(nodeData.errors() == 1);
        }

    }
}
