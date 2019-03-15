package io.triada.commands;

import com.google.common.net.HostAndPort;
import io.triada.commands.merge.MergeCommand;
import io.triada.commands.pay.PayCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.mocks.FakeKeys;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TestMergeCommand extends Assert {

    public final FakeHome fakeHome = new FakeHome();

    private final FakeKeys fakeKeys = new FakeKeys();

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testMergesWallet() throws Exception {
        final Wallet wallet = this.fakeHome.createEagerWallet();
        final Wallet first = this.fakeHome.createEagerWallet(wallet);
        Files.write(first.file().toPath(), Files.readAllBytes(wallet.file().toPath()));
        final Wallet second = this.fakeHome.createEagerWallet(wallet);
        Files.write(second.file().toPath(), Files.readAllBytes(wallet.file().toPath()));
        new PayCommand(
                new Wallets(wallet.file().getParentFile()),
                new RemoteNodes(this.folder.newFile()),
                new CopiesFromFile(wallet.file().getParentFile().toPath())
        ).run(new String[]{
                "-pay",
                "private-key=" + ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")).getAbsolutePath(),
                "force",
                "payer=" + wallet.head().id(),
                "recipient=" + "NOPREFIX@" + new LongId().asText(),
                "amount=" + new TxnAmount(new BigDecimal("14.95")).asText(2),
                "details=" + "For the car"

        });
        final Path copiesPath = this.folder.newFolder(wallet.head().id()).toPath();
        final CopiesFromFile copies = new CopiesFromFile(copiesPath);
        copies.add(FileUtils.readFileToString(first.file(), StandardCharsets.UTF_8), HostAndPort.fromParts("host-1", 80), 5);
        copies.add(FileUtils.readFileToString(first.file(), StandardCharsets.UTF_8), HostAndPort.fromParts("host-2", 80), 5);

        final List<String> modified = new MergeCommand(
                new Wallets(wallet.file().getParentFile()),
                new RemoteNodes(this.folder.newFile()),
                copiesPath.getParent()
        ).run(
                new String[]{
                        "-merge",
                        "ids=" + wallet.head().id()
                }
        );
        assertEquals(1, modified.size());
        assertEquals(wallet.head().id(), modified.get(0));
    }

    @Test
    public void testMergeCopyOnTop() throws Exception {
        final Wallet wallet = this.fakeHome.createEagerWallet(LongId.ROOT);
        final Path copiesPath = this.folder.newFolder(wallet.head().id()).toPath();
        final CopiesFromFile copies = new CopiesFromFile(copiesPath);
        copies.add(FileUtils.readFileToString(wallet.file(), StandardCharsets.UTF_8), HostAndPort.fromParts("good-host", 80), 5);
        final RsaKey key = fakeKeys.privateKey();
        wallet.substract(
                new TxnAmount(new BigDecimal("9.99")),
                "NOPREFIX",
                new LongId(),
                key
        );
        new MergeCommand(
                new Wallets(wallet.file().getParentFile()),
                new RemoteNodes(this.folder.newFile()),
                copiesPath.getParent()
        ).run(
                new String[]{
                        "-merge",
                        "ids=" + wallet.head().id()
                }
        );
        assertTrue(wallet.balance().less(0L));
    }
}
