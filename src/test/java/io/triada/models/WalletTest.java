package io.triada.models;

import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.transaction.ValidatedTxn;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Repeat;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;
import java.util.Date;

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

    //TODO should be 39.99*2 not 79.97
    @Test
    public void testSubTxn() throws Exception {
        final Wallet wallet = fakeHome.createWallet(new LongId(), 0);
        final TxnAmount amount = new TxnAmount(new BigDecimal("39.99"));
        final RsaKey key = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        final Wallet sub = wallet.substract(
                amount,
                "NOPREFIX",
                new LongId(),
                key,
                "-"
        ).substract(
                amount,
                "NOPREFIX",
                new LongId(),
                key,
                "-"
        );

        assertThat(sub.balance().asText(2), is("-79.97"));
    }

    @Test
    public void testWalletAge() throws Exception {
        final Wallet wallet = fakeHome.createWallet(new LongId(), 0);
        final RsaKey key = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        final long hours = 100;
        final Wallet added = wallet.add(
                new SignedTriadaTxn(
                        new ValidatedTxn(
                                "0001",
                                new Date(System.currentTimeMillis() - (1000 * 60 * 60 *hours)),
                                new TxnAmount(new BigDecimal("1.99")),
                                "MOPREFIX",
                                new LongId(),
                                "-"
                        ),
                        key,
                        new LongId(wallet.head().id())
                )
        );
        assertEquals(hours, added.age());

    }


    @Test
    public void testAddTxb() throws Exception {
        final Wallet wallet = fakeHome.createWallet(new LongId(), 0);
        final TxnAmount amount = new TxnAmount(new BigDecimal("39.99"));
        final RsaKey key = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        final Wallet added = wallet.add(
                new SignedTriadaTxn(
                        new ValidatedTxn(
                                "0001",
                                new Date(),
                                amount,
                                "NOPREFIX",
                                new LongId(),
                                "-"
                        ),
                        key,
                        new LongId(wallet.head().id())

                )
        );
        assertThat(added.balance().asText(2), is("39.98"));
    }
}
