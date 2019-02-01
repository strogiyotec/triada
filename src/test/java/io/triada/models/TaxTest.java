package io.triada.models;

import io.triada.dates.DateConverters;
import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.tax.Tax;
import io.triada.models.tax.TransactionTaxes;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.transaction.Transaction;
import io.triada.models.transaction.ValidatedTxn;
import io.triada.models.wallet.Wallet;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;
import java.util.Date;

public final class TaxTest extends Assert {

    private static final FakeHome fakeHome = new FakeHome();


    @Test
    public void testTaxForOneYear() throws Exception{
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
        final TransactionTaxes transactionTaxes = new TransactionTaxes(added);
        assertEquals(TransactionTaxes.FEE.value() *hours,transactionTaxes.debt());
    }



}
