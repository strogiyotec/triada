package io.triada.commands;

import io.triada.commands.remote.RemoteCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.node.Farm;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class TestRemote extends Assert {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testUpdateRemote() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        final RemoteCommand remoteCommand = new RemoteCommand(nodes, new Farm.Empty());
        remoteCommand.run(new String[]{"-rclean"});

        assertTrue(nodes.all().isEmpty());

        remoteCommand.run(new String[]{"-radd", "localhost", String.valueOf(RemoteNodes.PORT), "-skip_ping"});

        remoteCommand.run(new String[]{"-radd", "localhost", "2222", "-skip_ping"});

        assertTrue(nodes.all().size() == 2);

    }

    @Test
    public void testAddDuplicates() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        final RemoteCommand remoteCommand = new RemoteCommand(nodes, new Farm.Empty());
        remoteCommand.run(new String[]{"-rclean"});

        assertTrue(nodes.all().isEmpty());

        remoteCommand.run(new String[]{"-radd", "localhost", "4444", "-skip_ping"});

        remoteCommand.run(new String[]{"-radd", "localhost", "4444", "-skip_ping"});

        assertTrue(nodes.all().size() == 1);

    }
}
