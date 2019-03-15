package io.triada.commands;

import io.triada.commands.create.CreateCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeKeys;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class TestCreateCommand extends Assert {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final FakeKeys fakeKeys = new FakeKeys();

    @Test
    public void testCreateWallet() throws Exception {
        final Wallets wallets = new Wallets(folder.newFolder());
        final String id = new CreateCommand(
                wallets,
                new RemoteNodes(folder.newFile())
        ).run(new String[]{
                "-create",
                "public-key=" + fakeKeys.publicKeyFile().getAbsolutePath(),
                "skip-test"
        });
        final Wallet wallet = wallets.acq(id);
        assertTrue(wallet.balance().zero());
        assertTrue(wallet.file().exists());
    }
}
