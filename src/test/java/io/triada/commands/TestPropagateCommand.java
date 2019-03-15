package io.triada.commands;

import io.triada.commands.pay.PayCommand;
import io.triada.commands.propagate.PropagateCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.mocks.FakeKeys;
import io.triada.models.amount.TxnAmount;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.math.BigDecimal;


public final class TestPropagateCommand extends Assert {

    private final FakeKeys keys = new FakeKeys();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    // TODO: 3/13/19 need to fix amount
    @Test
    public void testPropagate() throws Exception {
        final FakeHome fakeHome = new FakeHome();
        final Wallet wallet = fakeHome.createEagerWallet();
        final Wallet friend = fakeHome.createEagerWallet(wallet);
        final TxnAmount amount = new TxnAmount(new BigDecimal("14.95"));
        final Wallets wallets = new Wallets(wallet.file().getParentFile());
        new PayCommand(
                wallets,
                new RemoteNodes(temporaryFolder.newFile("remotes3")),
                new CopiesFromFile(wallet.file().getParentFile().toPath())
        ).run(new String[]{
                "-pay",
                "private-key=" + this.keys.privateKeyFile().getAbsolutePath(),
                "force",
                "payer=" + wallet.head().id(),
                "recipient=" + friend.head().id(),
                "amount=" + amount.asText(2),
                "details=" + "For the car"
        });
        new PropagateCommand(new EagerWallets(wallets.dir())).run(
                new String[]{
                        "-propagate",
                        "ids=" + wallet.head().id()
                });
        assertEquals("14.93", friend.balance().asText(2));
        assertEquals(1, friend.transactions().size());
    }
}
