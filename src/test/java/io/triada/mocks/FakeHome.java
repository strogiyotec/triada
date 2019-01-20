package io.triada.mocks;

import io.triada.models.id.WalletId;
import io.triada.models.key.RsaKey;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Fake home directory
 */
@Slf4j
public final class FakeHome {


    public Wallet createWallet(final WalletId id, final int txns) throws IOException {
        final File tempFile = File.createTempFile("/tmp/", TriadaWallet.EXT);
        tempFile.deleteOnExit();
        try (final FileWriter writer = new FileWriter(tempFile)) {
            writer.append(//head
                    String.join(
                            System.lineSeparator(),
                            "test",
                            "322",
                            id.toString(),
                            new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/id_rsa.pub"))).asPublic()
                    )
            );
        }
        return new TriadaWallet(tempFile);
    }
}
