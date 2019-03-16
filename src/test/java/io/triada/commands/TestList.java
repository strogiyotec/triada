package io.triada.commands;

import io.triada.commands.list.ListCommand;
import io.triada.mocks.FakeKeys;
import io.triada.models.id.LongId;
import io.triada.models.wallet.EmptyWallet;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class TestList extends Assert {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final FakeKeys fakeKeys = new FakeKeys();

    @Test
    public void testListsWalletsWithBalance() throws Exception {
        final LongId id = new LongId();
        final Wallets wallets = new Wallets(this.folder.newFolder());
        final EmptyWallet wallet = new EmptyWallet(
                id.asText(),
                this.fakeKeys.publicKey().asPublic(),
                wallets.dir().toPath().resolve(id + TriadaWallet.EXT).toFile().getAbsolutePath()
        );
        new ListCommand(
                wallets,
                this.path(
                        wallet.file().getParentFile(),
                        "copies"
                )
        ).run(new String[]{});
    }

    private Path path(final File parentFile, final String copies) throws IOException {
        return this.folder.newFolder(parentFile.toPath().resolve(copies).toFile().getAbsolutePath().split("/")).toPath();
    }
}
