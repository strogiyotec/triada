package io.triada.commands;

import io.triada.mocks.FakeHome;
import io.triada.mocks.FakeNode;
import io.triada.models.wallet.Wallet;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public final class TestNodeCommand {

    @Rule
    private final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final FakeHome fakeHome = new FakeHome();

    public void testNode() throws Exception {
        final Wallet wallet = this.fakeHome.createEagerWallet();

        new FakeNode(
                this.temporaryFolder.newFile("remotes"),
                wallet.file().getParentFile(),
                wallet.file().getParentFile()
        ).run(new String[]{});
    }
}
