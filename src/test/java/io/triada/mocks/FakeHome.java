package io.triada.mocks;

import io.triada.models.id.LongId;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import lombok.extern.slf4j.Slf4j;

/**
 * Fake home directory
 */
@Slf4j
public final class FakeHome {

    /**
     *
     * @param id Id of transaction
     * @param txns amount of txns
     * @return new Wallet
     * @throws Exception if failed
     */
    public Wallet createWallet(final LongId id, final int txns) throws Exception {
        return new TriadaWallet(
                new FakeHeadFile().fakeHome(id)
        );
    }
}
