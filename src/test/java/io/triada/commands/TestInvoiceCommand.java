package io.triada.commands;

import io.triada.commands.invoice.InvoiceCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public final class TestInvoiceCommand extends Assert {

    private final FakeHome fakeHome = new FakeHome();

    @Test
    public void testGenerateInvoice() throws Exception {
        final Wallet wallet = this.fakeHome.createWallet();
        final File walletDir = wallet.file().getParentFile();
        final String invoice = new InvoiceCommand(
                new Wallets(walletDir),
                new RemoteNodes(walletDir),
                new CopiesFromFile(walletDir.toPath())
        ).run(new String[]{
                "-invoice",
                "receiver=" + wallet.head().id(),
                "length=" + 16
        });

        assertEquals(33, invoice.length());
    }

    @Test
    public void testGenerateInvoiceWithDefaultLength() throws Exception {
        final Wallet wallet = this.fakeHome.createWallet();
        final File walletDir = wallet.file().getParentFile();
        final String invoice = new InvoiceCommand(
                new Wallets(walletDir),
                new RemoteNodes(walletDir),
                new CopiesFromFile(walletDir.toPath())
        ).run(new String[]{
                "-invoice",
                "receiver=" + wallet.head().id()
        });

        assertEquals(25, invoice.length());
    }
}
