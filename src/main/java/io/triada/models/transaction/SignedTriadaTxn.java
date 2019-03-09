package io.triada.models.transaction;

import io.triada.models.id.Id;
import io.triada.models.key.Key;
import io.triada.models.sign.TxnSignature;

import java.util.Objects;

public final class SignedTriadaTxn implements SignedTransaction {

    /**
     * Transaction to be signed
     */
    private final Transaction origin;

    /**
     * Signature of txn
     */
    private final String signature;

    /**
     * @param origin   The origin txn
     * @param prvtKey  Private key of wallet
     * @param walletId Owner wallet id
     * @throws Exception If failed
     */
    public SignedTriadaTxn(final Transaction origin, final Key prvtKey, final Id<Long> walletId) throws Exception {
        this.origin = origin;
        this.signature = new TxnSignature().sign(prvtKey, walletId, origin);
    }

    /**
     * @param origin    The origin txn
     * @param signature Signture
     */
    public SignedTriadaTxn(final Transaction origin, final String signature) {
        this.origin = origin;
        this.signature = signature;
    }

    @Override
    public Transaction origin() {
        return this.origin;
    }

    @Override
    public String signature() {
        return this.signature;
    }

    @Override
    public String asText() {
        return new ParsedTxnData(this).asText();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj instanceof SignedTransaction && Objects.equals(new ParsedTxnData(this), new ParsedTxnData((SignedTransaction) obj));
    }
}
