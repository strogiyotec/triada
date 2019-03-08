package io.triada.commands;

import io.triada.commands.pay.PayCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.math.BigDecimal;

// TODO: 3/8/19 Need merge command
public final class TestPropagateCommand extends Assert {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    public void testPropagate() throws Exception {
        final FakeHome fakeHome = new FakeHome();
        final Wallet wallet = fakeHome.createWallet();
        final Wallet friend = fakeHome.createWallet();
        final TxnAmount amount = new TxnAmount(new BigDecimal("14.95"));
        new PayCommand(
                new Wallets(wallet.file().getParentFile()),
                new RemoteNodes(temporaryFolder.newFile("remotes3"))
        ).run(new String[]{
                "pay",
                "wallet="+wallet.head().id(),
        });
    }
}
