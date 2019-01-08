package io.triada.models.transaction;

import io.triada.models.id.Id;
import io.triada.models.id.WalletId;
import io.triada.models.key.Key;
import io.triada.models.sign.TriadaSignature;

public final class SignedTriadaTxn implements SignedTransaction {

    /**
     * Transaction to be signed
     */
    private final Transaction origin;

    /**
     * Private key to sign
     */
    private final Key prvtKey;

    /**
     * Paying wallet id
     */
    private final Id<Long> walletId;

    private final String signature;

    /**
     * @param origin   The origin txn
     * @param prvtKey  Private key
     * @param walletId Wallet Id
     * @throws Exception If failed
     */
    public SignedTriadaTxn(final Transaction origin, final Key prvtKey, final Id<Long> walletId) throws Exception {
        this.origin = origin;
        this.prvtKey = prvtKey;
        this.walletId = walletId;
        this.signature = new TriadaSignature().sign(prvtKey, walletId, origin);
    }

    @Override
    public Transaction origin() {
        return this.origin;
    }

    @Override
    public String signature() {
        return this.signature;
    }
}
