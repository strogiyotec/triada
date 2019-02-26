package io.triada.models;

import com.google.common.net.HostAndPort;
import io.triada.dates.DateConverters;
import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.Wallet;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class TestCopies extends Assert {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testAddAndRemove() throws Exception {
        final File file = temporaryFolder.newFolder();
        final CopiesFromFile copies = new CopiesFromFile(file.toPath());
        copies.add(this.content("alpha"), HostAndPort.fromParts("192.168.0.1", 80), 1);
        copies.add(this.content("beta"), HostAndPort.fromParts("192.168.0.2", 80), 3);
        copies.add(this.content("beta"), HostAndPort.fromParts("192.168.0.3", 80), 7);
        copies.add(this.content("alpha"), HostAndPort.fromParts("192.168.0.4", 80), 10);
        copies.add(this.content("hello-to-delete"), HostAndPort.fromParts("192.168.0.5", 80), 10);


        copies.remove(HostAndPort.fromParts("192.168.0.5", 80));
        copies.clean();
        assertEquals(2, copies.all().size());
        assertEquals(
                copies.all()
                        .stream()
                        .filter(csv -> csv.name().equals("1"))
                        .findFirst()
                        .get()
                        .score(),
                11
        );

    }

    private String content(final String text) throws Exception {
        final LongId id = new LongId("aaaabbbbccccdddd");
        final Wallet wallet = new FakeHome().createWallet(id, 0);
        final TxnAmount amount = new TxnAmount(new BigDecimal(1.99));
        final RsaKey key = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        wallet.substract(amount, "NOPREFIX", new LongId("0000111122223333"), key, text, DateConverters.fromIso("2018-01-01T01:01:01Z"));
        return new String(Files.readAllBytes(wallet.file().toPath()), StandardCharsets.UTF_8);
    }
}
