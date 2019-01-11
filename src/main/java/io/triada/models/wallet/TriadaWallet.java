package io.triada.models.wallet;

import io.triada.models.key.Key;

import java.io.File;

public final class TriadaWallet implements Wallet {

    /**
     * The extension of the wallet file
     */
    private static final String EXT = ".trd";

    private final File file;

    public TriadaWallet(final File file) {
        if (!file.getAbsolutePath().contains(EXT)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Wallet file must end with %s",
                            EXT
                    )
            );
        }
        this.file = file;

    }

    @Override
    public Key walletKey() {
        return null;
    }
}
