package io.triada.models;

import com.google.common.net.HostAndPort;
import io.triada.dates.DateConverters;
import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.score.TriadaScore;
import io.triada.models.tax.TxnTaxes;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.transaction.ValidatedTxn;
import io.triada.models.wallet.Wallet;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;
import java.util.Arrays;

public final class TaxTest extends Assert {

    private static final FakeHome fakeHome = new FakeHome();

    @Test
    public void testTaxForOneYear() throws Exception {
        final Wallet wallet = fakeHome.createWallet(new LongId(), 0);
        final TxnAmount amount = new TxnAmount(new BigDecimal("19.99"));
        final RsaKey key = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        final int hours = 8760;
        final Wallet added = wallet.add(
                new SignedTriadaTxn(
                        new ValidatedTxn(
                                "0001",
                                DateConverters.nowMinusHours(hours),
                                amount,
                                "NOPREFIX",
                                new LongId(),
                                "-"
                        ),
                        key,
                        new LongId(wallet.head().id())

                )
        );
        final TxnTaxes transactionTaxes = new TxnTaxes(added);
        assertEquals(TxnTaxes.FEE.value() * hours, transactionTaxes.debt());
    }

    @Test
    public void testCalculateDebt() throws Exception {
        Wallet wallet = fakeHome.createWallet(new LongId(), 0);
        final RsaKey key = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        for (int i = 0; i < 30; i++) {
            wallet = wallet.add(
                    new SignedTriadaTxn(
                            new ValidatedTxn(
                                    "0001",
                                    DateConverters.nowMinusYears(10),
                                    new TxnAmount(new BigDecimal("1")),
                                    "NOPREFIX",
                                    new LongId(),
                                    "-"
                            ),
                            key,
                            new LongId(wallet.head().id())
                    )
            );
        }
        final TriadaScore score = new TriadaScore(
                HostAndPort.fromParts("localhost", 8080),
                "NOPREFIX@cccccccccccccccc",
                Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V")
        );
        final TxnTaxes txnTaxes = new TxnTaxes(wallet);
        final long debt = txnTaxes.debt();
        final TxnTaxes pay = txnTaxes.pay(key, score);
        assertEquals(debt, new ParsedTxnData(pay.last().get()).amount().value() * -1);
    }


}
