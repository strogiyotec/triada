package io.triada.commands;

import io.triada.commands.fetch.FetchCommand;
import io.triada.commands.push.PushCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.mocks.FakeNode;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallet;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TestNodeCommand extends Assert {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final FakeHome fakeHome = new FakeHome();

    @Test
    public void testNode() throws Exception {
        final Wallet wallet = this.fakeHome.createEagerWallet();

        final Integer port = new FakeNode(
                this.temporaryFolder.newFile("remotes"),
                wallet.file().getParentFile(),
                wallet.file().getParentFile(),
                wallet.file().getParentFile().toString()
        ).run(new String[]{});

        final RemoteNodes remoteNodes = new RemoteNodes(this.temporaryFolder.newFile("remotes2"));
        remoteNodes.add("localhost", port);

        new PushCommand(
                new EagerWallets(wallet.file().getParentFile()),
                remoteNodes
        ).run(new String[]{
                "-push",
                "ignore-score-weakness",
                "tolerate-edges",
                "tolerate-quorum=1"
        });

        final File copies = this.copies(wallet);
        new FetchCommand(
                wallet.file().getParentFile().toPath(),
                copies.toPath(),
                remoteNodes
        ).run(
                new String[]{
                        "-fetch",
                        "ignore-score-weakness",
                        "tolerate-edges",
                        "tolerate-quorum=1"
                }
        );
        final CopiesFromFile copiesFromFile = new CopiesFromFile(copies.toPath());

        assertEquals(1, copiesFromFile.all().size());
        assertEquals("1", copiesFromFile.all().get(0).name());
    }

    private File copies(final Wallet wallet) throws IOException {
        final String[] dirs = wallet.file().getParentFile().toString().split("/");
        final List<String> copiePath = new ArrayList<>(dirs.length + 2);
        copiePath.addAll(Arrays.asList(dirs));
        copiePath.add("copies");
        copiePath.add(wallet.head().id());

        return this.temporaryFolder.newFolder(copiePath.toArray(new String[]{}));
    }
}
