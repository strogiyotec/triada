package io.triada.mocks;

import io.triada.models.id.LongId;
import io.triada.models.wallet.EagerWallet;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

/**
 * Fake home directory
 */
@Slf4j
public final class FakeHome {

    /**
     * @param id   Id of transaction
     * @param txns amount of txns
     * @return new Wallet
     * @throws Exception if failed
     */
    public Wallet createWallet(final LongId id, final int txns) throws Exception {
        return new TriadaWallet(
                new FakeHeadFile().fakeHome(id)
        );
    }

    public Wallet createWallet() throws Exception {
        return new TriadaWallet(
                new FakeHeadFile().fakeHome(new LongId())
        );
    }

    public Wallet createEagerWallet() throws Exception {
        return new EagerWallet(
                new FakeHeadFile().fakeHome(new LongId())
        );
    }

    public Wallet createEagerWallet(final LongId id) throws Exception {
        return new EagerWallet(
                new FakeHeadFile().fakeHome(id)
        );
    }

    public Wallet createEagerWallet(final LongId id, final Wallet origin) throws Exception {
        return new EagerWallet(
                new FakeHeadFile().fakeHome(id, origin)
        );
    }

    public Wallet createWallet(final Wallet origin) throws Exception {
        return new TriadaWallet(
                new FakeHeadFile().fakeHome(new LongId(), origin)
        );
    }

    public Wallet createEagerWallet(final Wallet origin) throws Exception {
        return new EagerWallet(
                new FakeHeadFile().fakeHome(new LongId(), origin)
        );
    }

    /**
     * @param amount Amount of wallets to create
     * @return List of files with wallet
     * @throws Exception if failed
     */
    public List<File> createWallets(final int amount) throws Exception {
        return new FakeHeadFile().fakeHomes(amount);
    }
}
