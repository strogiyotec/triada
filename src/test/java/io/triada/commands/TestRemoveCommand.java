package io.triada.commands;

import io.triada.commands.remove.RemoveCommand;
import io.triada.mocks.FakeFile;
import io.triada.mocks.FakeHome;
import io.triada.models.id.LongId;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public final class TestRemoveCommand extends Assert {

    @Test
    public void testRemoveOneWallet() throws Exception {
        final Wallet wallet = new FakeHome().createWallet(new LongId(), 0);
        final File parentFile = wallet.file().getParentFile();
        final File[] before = parentFile.listFiles(file -> file.getName().endsWith(TriadaWallet.EXT));
        assertTrue(before.length != 0);

        new RemoveCommand(parentFile).run(new String[]{"-remove", wallet.head().id()});

        final File[] after = parentFile.listFiles(file -> file.getName().endsWith(TriadaWallet.EXT));
        assertTrue(after.length == 0);
    }

    @Test
    public void testRemoveAllWallets() throws Exception {
        final List<File> wallets = new FakeHome().createWallets(2);
        final File parentFile = wallets.get(0).getParentFile();
        final File[] before = parentFile.listFiles(file -> file.getName().endsWith(TriadaWallet.EXT));
        assertTrue(before.length == 2);

        new RemoveCommand(parentFile).run(new String[]{"-remove"});

        final File[] after = parentFile.listFiles(file -> file.getName().endsWith(TriadaWallet.EXT));
        assertTrue(after.length == 0);
    }

    @Test
    public void testRemoveAbsentWallet() throws Exception {
        final File tempFile = new FakeFile(".txt").call();
        final File parentFile = tempFile.getParentFile();

        new RemoveCommand(parentFile).run(new String[]{"-remove","ffdsfsdf"});
        final File[] files = parentFile.listFiles(file -> file.getName().endsWith(TriadaWallet.EXT));
        assertTrue(files.length == 0);
    }
}
