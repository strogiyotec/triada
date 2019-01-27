package io.triada.models.tax;

import io.triada.models.amount.TxnAmount;
import io.triada.models.wallet.Wallet;

public final class TransactionTaxes implements Tax {

    /**
     * This is how much we charge per one transaction per hour
     * of storage. A wallet of 4096 transactions will pay
     * approximately 16TRIADZ per year.
     * Here is the formula: 16.0 / (365 * 24) / 4096 = 1915
     * But I like the 1917 number better.
     */
    private static final TxnAmount FEE = new TxnAmount(1917L);

    private final Wallet wallet;

    private final boolean ignoreScoreWeakness;

    private final int strength;

    public TransactionTaxes(
            final Wallet wallet,
            final boolean ignoreScoreWeakness,
            final int strength
    ) {
        this.wallet = wallet;
        this.ignoreScoreWeakness = ignoreScoreWeakness;
        this.strength = strength;
    }

    @Override
    public void pay() throws Exception {

    }

    @Override
    public void paid() throws Exception {

    }

    @Override
    public long debt() throws Exception {
        return FEE.value() * this.wallet.transactions().size();
    }

    @Override
    public String asText() {
        return null;
    }
}
