package io.triada.models;

import com.google.common.collect.Iterables;
import io.triada.dates.DateConverters;
import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.patch.TxnsPatch;
import io.triada.models.prefix.PaymentPrefix;
import io.triada.models.transaction.InversedTxn;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.transaction.ValidatedTxn;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

public final class TestPatch extends Assert {

    private final FakeHome fakeHome = new FakeHome();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final String PREFIX = "NOPREFIX";

    @Test
    public void testBuildsPatch() throws Exception {
        final Wallet first = this.fakeHome.createWallet();
        final Wallet second = this.fakeHome.createEagerWallet(first);
        final Wallet third = this.fakeHome.createEagerWallet(first);
        final RsaKey rsaKey = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));

        Files.write(second.file().toPath(), Files.readAllBytes(first.file().toPath()));

        first.substract(new TxnAmount(new BigDecimal("39.0")), PREFIX + "@" + new LongId().asText(), rsaKey, "-");
        first.substract(new TxnAmount(new BigDecimal("11.0")), PREFIX + "@" + new LongId().asText(), rsaKey, "-");
        first.substract(new TxnAmount(new BigDecimal("3.0")), PREFIX + "@" + new LongId().asText(), rsaKey, "-");
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
        patch.join(first, (signedTransaction) -> false);
        patch.join(second, (signedTransaction) -> false);
        patch.join(third, (signedTransaction) -> false);

        assertTrue(patch.save(first.file(), true));
        assertEquals("-53.00", first.balance().asText(2));
    }

    @Test
    public void testAcceptNegativeBalanceInRootWallet() throws Exception {
        final Wallet first = this.fakeHome.createEagerWallet(LongId.ROOT);
        final Wallet second = this.fakeHome.createEagerWallet(first);
        Files.write(second.file().toPath(), Files.readAllBytes(first.file().toPath()));
        final RsaKey rsaKey = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        final TxnAmount amount = new TxnAmount(new BigDecimal("333.0"));
        second.substract(amount, PREFIX, new LongId(), rsaKey);
        final TxnsPatch patch = new TxnsPatch(new Wallets(first.file().getParentFile()));
        patch.join(first, (signedTransaction) -> false);
        patch.join(second, (signedTransaction) -> false);

        assertTrue(patch.save(first.file(), true));
        assertEquals(amount.mpy(-1L).asText(2), first.balance().asText(2));
    }

    @Test
    public void testMergeFragmentedParts() throws Exception {
        final Wallet first = this.fakeHome.createEagerWallet(LongId.ROOT);
        final Wallet second = this.fakeHome.createEagerWallet(first);
        Files.write(second.file().toPath(), Files.readAllBytes(first.file().toPath()));
        final RsaKey rsaKey = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        final Date start = DateConverters.fromIso("2017-07-19T21:24:51Z");
        first.add(
                new SignedTriadaTxn(
                        new ValidatedTxn(
                                "1",
                                start,
                                new TxnAmount(new BigDecimal("-2.0")),
                                new PaymentPrefix(first).create(),
                                new LongId(),
                                "firstPayment"
                        ),
                        rsaKey,
                        new LongId(first.head().id())
                )
        );
        second.add(
                new SignedTriadaTxn(
                        new ValidatedTxn(
                                "2",
                                start,
                                new TxnAmount(new BigDecimal("-2.0")),
                                new PaymentPrefix(first).create(),
                                new LongId(),
                                "secondPayment"
                        ),
                        rsaKey,
                        new LongId(first.head().id())
                )
        );
        first.add(
                new SignedTriadaTxn(
                        new ValidatedTxn(
                                "3",
                                start,
                                new TxnAmount(new BigDecimal("-2.0")),
                                new PaymentPrefix(first).create(),
                                new LongId(),
                                "thirdPayment"
                        ),
                        rsaKey,
                        new LongId(first.head().id())
                )
        );
        final TxnsPatch patch = new TxnsPatch(new Wallets(first.file().getParentFile()));
        patch.join(first, (signedTransaction) -> false);
        patch.join(second, (signedTransaction) -> false);
        assertTrue(patch.save(first.file(), true));
        assertEquals(3, first.transactions().size());
        assertEquals("-6.00", first.balance().asText(2));
    }

    @Test
    public void testProtocolsNewTxns() throws Exception {
        final Wallet first = this.fakeHome.createEagerWallet(LongId.ROOT);
        final Wallet second = this.fakeHome.createEagerWallet(first);
        Files.write(second.file().toPath(), Files.readAllBytes(first.file().toPath()));
        final RsaKey rsaKey = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        final TxnAmount amount = new TxnAmount(new BigDecimal("333.0"));
        final LongId target = new LongId();

        second.substract(amount, PREFIX, target, rsaKey, "some details");
        second.substract(amount.mpy(2L), PREFIX, target, rsaKey);

        final TxnsPatch patch = new TxnsPatch(new Wallets(first.file().getParentFile()));
        patch.legacy(first);

        final File file = this.temporaryFolder.newFile();

        patch.join(second, file, (signedTransaction) -> false);
        final List<String> lines = Files.readAllLines(file.toPath());
        assertEquals(2, lines.size());

        final String[] parts = lines.get(0).split(";");
        assertTrue(DateConverters.isUnix(parts[0]));
        assertEquals("1", parts[1]);
        assertTrue(DateConverters.isUnix(parts[2]));
        assertEquals(LongId.ROOT.asText(), parts[3]);
        assertEquals(target.asText(), parts[4]);
        assertEquals(amount.asText(2), new TxnAmount(new BigDecimal(parts[5])).asText(2));
        assertEquals(PREFIX, parts[6]);
        assertEquals("some details", parts[7]);
    }
}