package io.triada.models.wallet;

import java.io.File;
import java.util.List;

public final class EagerWallets {

    private final Wallets wallets;

    public EagerWallets(final File dir) {
        this.dir = dir;
        this.wallets = new Wallets(dir);
    }

    private final File dir;

    /**
     * @return List of wallets IDs
     */
    public List<String> all() {
        return this.wallets.all();
    }

    public File dir() {
        return this.dir;
    }

    /**
     * @return Amount of wallets
     */
    public int count() {
        return this.wallets.count();
    }

    /**
     * @param id Wallet id
     * @return True if wallet file exisrs
     * @throws Exception if failed
     */
    public boolean exists(String id) throws Exception {
        return this.wallets.exists(id);
    }

    /**
     * @param id Wallet id
     * @return Wallet with give id
     * @throws Exception if failed
     */
    public Wallet acq(final String id) throws Exception {
        return new EagerWallet(this.dir.toPath().resolve(id + TriadaWallet.EXT).toFile());
    }
}
