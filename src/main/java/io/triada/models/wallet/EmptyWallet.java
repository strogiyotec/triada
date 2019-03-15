package io.triada.models.wallet;

import io.triada.Triada;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Create wallet with 0 txns
 */
// TODO: 3/10/19 Add test
public final class EmptyWallet extends WalletEnvelope {
    // TODO: 3/9/19 Add network validation
    public EmptyWallet(
            final String id,
            final String pubKey,
            final String network,
            final boolean overwrite,
            final String path
    ) {
        super(Unchecked.supplier(() -> {
            final File file = new File(path);
            if (file.exists() && !overwrite) {
                throw new IllegalStateException(
                        String.format(
                                "File %s already exists",
                                path
                        )
                );
            }
            file.createNewFile();
            final String head = String.join(
                    System.lineSeparator(),
                    network,
                    Triada.PROTOCOL,
                    id,
                    pubKey,
                    "\r"
            );
            Files.write(
                    file.toPath(),
                    head.getBytes(StandardCharsets.UTF_8)
            );
            return new TriadaWallet(file);
        }));
    }

    /**
     * Create empty wallet with test network
     */
    public EmptyWallet(
            final String id,
            final String pubKey,
            final String path
    ) {
        this(id, pubKey, "test", false, path);
    }

    public EmptyWallet(
            final String id,
            final String pubKey,
            final String network,
            final String path
    ) {
        this(id, pubKey, network, false, path);
    }
    public EmptyWallet(
            final String id,
            final String pubKey,
            final String network,
            final File path
    ) {
        this(id, pubKey, network, false, path.getAbsolutePath());
    }
}
