package io.triada.text;

import io.triada.models.wallet.Wallet;
import lombok.experimental.Delegate;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Get content of wallet's file
 */
public final class WalletContent {

    /**
     * Wallet content
     */
    @Delegate
    private final String origin;

    /**
     * Ctor
     *
     * @param wallet Wallet with file direction
     * @throws IOException if failed
     */
    public WalletContent(final Wallet wallet) throws IOException {
        this.origin = FileUtils.readFileToString(wallet.file(), StandardCharsets.UTF_8);
    }
}
