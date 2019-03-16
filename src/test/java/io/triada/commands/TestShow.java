package io.triada.commands;

import io.triada.commands.show.ShowCommand;
import io.triada.mocks.FakeKeys;
import io.triada.models.amount.Amount;
import io.triada.models.amount.TxnAmount;
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

public final class TestShow extends Assert {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final FakeKeys fakeKeys = new FakeKeys();

    @Test
    public void testCheckWalletBalance() throws Exception {
        final String id = new LongId().asText();
        final Wallets wallets = new Wallets(this.folder.newFolder());
        final EmptyWallet wallet = new EmptyWallet(
                id,
                this.fakeKeys.publicKey().asPublic(),
                wallets.dir().toPath().resolve(id + TriadaWallet.EXT).toFile().getAbsolutePath()
        );
        final Amount<Long> balance = new ShowCommand(
                wallets,
                this.path(
                        wallet.file().getParentFile(),
                        "copies"
                )
        ).run(new String[]{
                "-show",
                "id=" + wallet.head().id()
        });
        assertEquals(TxnAmount.ZERO.value(), balance.value());
    }

    private Path path(final File parentFile, final String copies) throws IOException {
        return this.folder.newFolder(parentFile.toPath().resolve(copies).toFile().getAbsolutePath().split("/")).toPath();
    }
}
