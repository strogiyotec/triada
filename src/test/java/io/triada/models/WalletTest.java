package io.triada.models;

import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Repeat;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;

public final class WalletTest extends Assert {
    private final FakeHome fakeHome = new FakeHome();

    @Test
    public void testEmptyWallet() throws Exception {
        final Wallet wallet = fakeHome.createWallet(new LongId(), 0);
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


    @Test
    public void testAddTxn() throws Exception {
        final Wallet wallet = fakeHome.createWallet(new LongId(), 0);
        final TxnAmount amount = new TxnAmount(new BigDecimal("39.99"));
        final RsaKey key = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/id_rsa")));

        assertThat(
                wallet.substract(amount,"NOPREFIX",new LongId(),key,"-").balance().value(),
                is(amount)
        );
    }
}
