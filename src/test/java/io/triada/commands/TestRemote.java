package io.triada.commands;

import io.triada.commands.remote.RemoteCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.node.farm.Farm;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class TestRemote extends Assert {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testClean() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        final RemoteCommand remoteCommand = new RemoteCommand(nodes, new Farm.Empty());
        remoteCommand.run(new String[]{"remote clean"});

        assertTrue(nodes.all().isEmpty());

    }
}
