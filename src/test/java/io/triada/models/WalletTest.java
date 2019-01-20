package io.triada.models;

import io.triada.mocks.FakeHome;
import io.triada.models.id.WalletId;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Repeat;
import org.springframework.util.ResourceUtils;

public final class WalletTest extends Assert {
    private final FakeHome fakeHome = new FakeHome();

    @Test
    public void testEmptyWallet() throws Exception {
        final Wallet wallet = fakeHome.createWallet(new WalletId(), 0);
        assertTrue(wallet.balance().zero());
        assertTrue(wallet.transactions().isEmpty());
    }

    @Test
    @Repeat(value = 10)
    public void testReadHugeWallet() throws Exception {
        final long before = System.currentTimeMillis();
        new TriadaWallet(
                ResourceUtils.getFile(
                        this.getClass().getResource("/wallet/448b451bc62e8e16.trd")
                )
        );
        final long period = System.currentTimeMillis() - before;
        assertTrue(period < 300);
    }

    public void testAddTxn() throws Exception {
        final Wallet wallet = fakeHome.createWallet(new WalletId(), 0);
    }
}
