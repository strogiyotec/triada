package io.triada.routine;

import io.triada.mocks.FakeHome;
import io.triada.mocks.FakeKeys;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallet;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public final class TestWalletGc extends Assert {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final FakeHome fakeHome = new FakeHome();

    private final FakeKeys fakeKeys = new FakeKeys();

    @Test
    public void testCollectsGarbage() throws Exception {
        final Wallet wallet = this.fakeHome.createEagerWallet();
        final EagerWallets wallets = new EagerWallets(wallet.file().getParentFile());
        final WalletGc routine = new WalletGc(
                Arrays.asList("routine-immediately", "gc-age=" + 0),
                wallets
        );
        routine.run();
        assertEquals(0, wallets.count());
    }

    @Test
    public void testDoesntTouchNonEmptyWallets() throws Exception {
        final Wallet wallet = this.fakeHome.createEagerWallet();
        final EagerWallets wallets = new EagerWallets(wallet.file().getParentFile());
        final TxnAmount amount = new TxnAmount(new BigDecimal("39.99"));
        final File file = fakeKeys.privateKeyFile();
        wallet.substract(amount, "NOPREFIX@" + new LongId().asText(), new RsaKey(file));
        final WalletGc routine = new WalletGc(
                Arrays.asList("routine-immediately", "gc-age=" + 0),
                wallets
        );
        routine.run();
        assertEquals(1, wallets.count());
    }


    @Test
    public void testDoesntTouchFreshWallets() throws Exception {
        final Wallet wallet = this.fakeHome.createEagerWallet();
        final EagerWallets wallets = new EagerWallets(wallet.file().getParentFile());
        final WalletGc routine = new WalletGc(
                Arrays.asList("routine-immediately", "gc-age=" + TimeUnit.HOURS.toMillis(1)),
                wallets
        );
        routine.run();
        assertEquals(1, wallets.count());
    }


}
