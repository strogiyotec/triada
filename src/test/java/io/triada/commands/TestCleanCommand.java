package io.triada.commands;

import com.google.common.net.HostAndPort;
import io.triada.commands.clean.CleanCommand;
import io.triada.dates.DateConverters;
import io.triada.mocks.FakeHome;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class TestCleanCommand extends Assert {

    private final FakeHome fakeHome = new FakeHome();

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCleansCopies() throws Exception {
        final Wallet wallet = this.fakeHome.createEagerWallet();
        final CopiesFromFile copies = new CopiesFromFile(this.path(wallet.file().getParentFile(), "copies/" + wallet.head().id()));
        copies.add("a1", HostAndPort.fromParts("host1", 80), 1, DateConverters.nowMinusHours(26));
        copies.add("a2", HostAndPort.fromParts("host2", 80), 2, DateConverters.nowMinusHours(26));
        new CleanCommand(
                copies.root().toPath(),
                new Wallets(wallet.file().getParentFile())
        ).run(new String[]{
                "-clean",
                "id=" + wallet.head().id()
        });
        assertTrue(copies.all().isEmpty());
    }

    @Test
    public void testCleanNoCopies() throws Exception {
        final Wallet wallet = this.fakeHome.createEagerWallet();
        final CopiesFromFile copies = new CopiesFromFile(this.path(wallet.file().getParentFile(), "copies/" + wallet.head().id()));
        new CleanCommand(
                copies.root().toPath(),
                new Wallets(wallet.file().getParentFile())
        ).run(new String[]{
                "-clean"
        });
        assertTrue(copies.all().isEmpty());
    }

    private Path path(final File parentFile, final String copies) throws IOException {
        return this.folder.newFolder(parentFile.toPath().resolve(copies).toFile().getAbsolutePath().split("/")).toPath();
    }
}
