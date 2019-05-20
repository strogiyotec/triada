package io.triada.models.wallet;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public final class Wallets {

    private final File dir;

    public Wallets(final EagerWallets wallets) {
        this.dir = wallets.dir();
    }

    /**
     * @return List of wallets IDs
     */
    public List<String> all() {
        return Stream.of(Objects.requireNonNull(this.dir.listFiles((dir, name) -> name.endsWith(TriadaWallet.EXT))))
                .map(file -> FilenameUtils.removeExtension(file.getName()))
                .collect(Collectors.toList());
    }

    public File dir() {
        return this.dir;
    }

    /**
     * @return Amount of wallets
     */
    public int count() {
        return Objects.requireNonNull(this.dir.listFiles((dir, name) -> name.endsWith(TriadaWallet.EXT))).length;
    }

    /**
     * @param id Wallet id
     * @return True if wallet file exists
     * @throws Exception if failed
     */
    public boolean exists(String id) throws Exception {
        return this.dir.toPath().resolve(id + TriadaWallet.EXT).toFile().exists();
    }

    /**
     * @param id Wallet id
     * @return Wallet with give id
     * @throws Exception if failed
     */
    public Wallet acq(final String id) throws Exception {
        return new TriadaWallet(this.dir.toPath().resolve(id + TriadaWallet.EXT).toFile());

    }
}
