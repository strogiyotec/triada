package io.triada.commands;

import io.triada.commands.pay.PayCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;

// TODO: 3/8/19 Need invoice command when invoice doesn't contain @
public final class TestPayCommand extends Assert {

    private final FakeHome fakeHome = new FakeHome();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    // TODO: 3/8/19 First assertion is broken , need to fix amount
    @Test
    public void testSendsFromWalletToWallet() throws Exception {
        Wallet source = this.fakeHome.createWallet();
        final Wallet target = this.fakeHome.createWallet(source);
        final TxnAmount amount = new TxnAmount(new BigDecimal("14.95"));
        //send money to target
        new PayCommand(
                new Wallets(source.file().getParentFile()),
                new RemoteNodes(temporaryFolder.newFile("remotes6")),
                new CopiesFromFile(target.file().getParentFile().toPath())
        ).run(new String[]{
                "-pay",
                "private-key=" + ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")).getAbsolutePath(),
                "force",
                "payer=" + source.head().id(),
                "recipient=" + target.head().id(),
                "amount=" + amount.asText(2),
                "details=" + "For the car"
        });
        source = new TriadaWallet(source.file());
        assertThat("-14.94", is(source.balance().asText(2)));
        //send money back to source
        System.out.println(source.head().id());
        new PayCommand(
                new Wallets(source.file().getParentFile()),
                new RemoteNodes(temporaryFolder.newFile("remotes7")),
                new CopiesFromFile(target.file().getParentFile().toPath())
        ).run(new String[]{
                "-pay",
                "private-key=" + ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")).getAbsolutePath(),
                "payer=" + target.head().id(),
                "recipient=" + source.head().id(),
                "amount=" + amount.asText(2),
                "details=" + "Thank you , sand you back "
        });
        assertEquals(
                TxnAmount.ZERO.value(),
                new TriadaWallet(source.file()).balance().value()
        );
    }
}
