package io.triada.mocks;

import com.google.common.io.Files;
import io.triada.Triada;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

//// TODO: 3/2/19 Maybe use TempFolder rule ? Need to check it
public final class FakeHeadFile {

    public File fakeHome(final LongId id) throws Exception {
        final File tempDir = Files.createTempDir();
        final File tempFile = new File(tempDir, id.toString() + TriadaWallet.EXT);
        tempDir.deleteOnExit();
        try (final FileWriter writer = new FileWriter(tempFile)) {
            final String head = String.join(
                    System.lineSeparator(),
                    Triada.TEST_NETWORK,
                    Triada.PROTOCOL,
                    id.asText(),
                    new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/id_rsa.pub"))).asPublic(),
                    "\r"
            );
            writer.append(head);
        }
        return tempFile;
    }

    public File fakeHome(final LongId id, final Wallet wallet) throws Exception {
        final File tempDir = Files.createTempDir();
        final File tempFile = new File(wallet.file().getParent(), id.toString() + TriadaWallet.EXT);
        tempDir.deleteOnExit();
        try (final FileWriter writer = new FileWriter(tempFile)) {
            final String head = String.join(
                    System.lineSeparator(),
                    Triada.TEST_NETWORK,
                    Triada.PROTOCOL,
                    id.asText(),
                    new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/id_rsa.pub"))).asPublic(),
                    "\r"
            );
            writer.append(head);
        }
        return tempFile;
    }

    public List<File> fakeHomes(final int amount) throws Exception {
        final File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        final List<File> wallets = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            final LongId id = new LongId();
            final File tempFile = new File(tempDir, id.toString() + TriadaWallet.EXT);
            try (final FileWriter writer = new FileWriter(tempFile)) {
                final String head = String.join(
                        System.lineSeparator(),
                        "test",
                        Triada.PROTOCOL,
                        id.toString(),
                        new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/id_rsa.pub"))).asPublic(),
                        "\r"
                );
                writer.append(head);
            }
            wallets.add(tempFile);
        }

        return wallets;
    }
}
