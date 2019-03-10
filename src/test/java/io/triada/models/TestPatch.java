package io.triada.models;

import com.google.common.collect.Iterables;
import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.patch.TxnsPatch;
import io.triada.models.transaction.InversedTxn;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;
import java.nio.file.Files;

public final class TestPatch extends Assert {

    private final FakeHome fakeHome = new FakeHome();

    private static final String PREFIX = "NOPREFIX";

    @Test
    public void testBuildsPatch() throws Exception {
        Wallet first = this.fakeHome.createWallet();
        final Wallet second = this.fakeHome.createEagerWallet(first);
        final Wallet third = this.fakeHome.createEagerWallet(first);
        final RsaKey rsaKey = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        Files.write(second.file().toPath(), Files.readAllBytes(first.file().toPath()));

        first.substract(new TxnAmount(new BigDecimal("39.0")), PREFIX + "@" + new LongId().asText(), rsaKey, "-");
        first.substract(new TxnAmount(new BigDecimal("11.0")), PREFIX + "@" + new LongId().asText(), rsaKey, "-");
        first = first.substract(new TxnAmount(new BigDecimal("3.0")), PREFIX + "@" + new LongId().asText(), rsaKey, "-");
        second.substract(new TxnAmount(new BigDecimal("44.0")), PREFIX + "@" + new LongId().asText(), rsaKey, "-");

        Files.write(third.file().toPath(), Files.readAllBytes(first.file().toPath()));
        final SignedTransaction txn = Iterables.getLast(third.substract(new TxnAmount(new BigDecimal("10.0")), PREFIX + "@" + new LongId().asText(), rsaKey, "-").transactions());
        third.add(
                new SignedTriadaTxn(
                        new InversedTxn(
                                txn,
                                new LongId()
                        ),
                        txn.signature()

                )
        );

        final TxnsPatch patch = new TxnsPatch(new Wallets(first.file().getParentFile()));
        patch.join(first, () -> false);
        patch.join(second, () -> false);
        patch.join(third, () -> false);

        assertTrue(patch.save(first.file(), true));
        assertEquals("-53.00", first.balance().asText(2));
    }
}
