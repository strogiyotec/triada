package io.triada.mocks;

import com.google.common.io.Files;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.wallet.TriadaWallet;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileWriter;

public final class FakeHeadFile {

    public File fakeHome(final LongId id) throws Exception {
        final File tempFile = new File(Files.createTempDir(),id.toString()+TriadaWallet.EXT);
        tempFile.deleteOnExit();
        try (final FileWriter writer = new FileWriter(tempFile)) {
            final String head = String.join(
                    System.lineSeparator(),
                    "test",
                    "322",
                    id.toString(),
                    new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/id_rsa.pub"))).asPublic(),
                    "\r"
            );
            writer.append(head);
        }
        return tempFile;
    }
}
