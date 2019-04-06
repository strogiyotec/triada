package io.triada.node.entrance;

import io.triada.commands.pay.PayCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.mocks.FakeKeys;
import io.triada.models.id.LongId;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TestBlockingEntrance extends Assert {

    private final FakeHome fakeHome = new FakeHome();

    private final FakeKeys fakeKeys = new FakeKeys();

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testPushedWallet() throws Exception {
        final LongId sid = LongId.ROOT;
        final LongId tid = new LongId();
        final Wallet source = this.fakeHome.createEagerWallet(sid);
        final Wallet target = this.fakeHome.createEagerWallet(tid, source);
        new PayCommand(
                new Wallets(source.file().getParentFile()),
                new RemoteNodes(this.folder.newFolder("remotes")),
                new CopiesFromFile(source.file().getParentFile().toPath())
        ).run(new String[]{
                "-pay",
                "force",
                "private-key=" + this.fakeKeys.privateKeyFile().getAbsolutePath(),
                "payer=" + source.head().id(),
                "recipient=" + target.head().id(),
                "amount=" + "19.99",
                "details=" + "testing"
        });
        final String body = FileUtils.readFileToString(source.file(), StandardCharsets.UTF_8);
        final File ledger = this.folder.newFile("ledger.csv");
        final Path copiesPath = this.path(
                source.file().getParentFile(),
                "copies"
        );
        final Path resolve = copiesPath.resolve(source.head().id());
        Files.createDirectory(resolve);
        resolve.toFile().deleteOnExit();
        final BlockingEntrance entrance = new BlockingEntrance(
                new Wallets(source.file().getParentFile()),
                new RemoteNodes(this.folder.newFile()),
                copiesPath,
                "x",
                ledger.toPath()
        );
        final List<String> modified = entrance.push(source.head().id(), body);

        assertEquals(1, modified.size());
        assertEquals("-19.99", source.balance().asText(2));
        assertEquals("19.98", target.balance().asText(2));
        assertEquals(1, Files.lines(ledger.toPath()).count());
    }

    private Path path(final File parentFile, final String copies) throws IOException {
        return this.folder.newFolder(parentFile.toPath().resolve(copies).toFile().getAbsolutePath().split("/")).toPath();
    }


}
