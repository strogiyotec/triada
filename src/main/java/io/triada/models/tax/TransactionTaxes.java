package io.triada.models.tax;

import io.triada.models.amount.TxnAmount;
import io.triada.models.score.IsValidScore;
import io.triada.models.score.TriadaScore;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.wallet.Wallet;

import java.math.BigDecimal;
import java.util.List;

public final class TransactionTaxes implements Tax {


    /**
     * The exact score a wallet must but in order to pay taxes
     */
    private static final int EXACT_SCORE = 8;

    private static final IsValidScore scoreValid = new IsValidScore();

    /**
     * This is how much we charge per one transaction per hour
     * of storage. A wallet of 4096 transactions will pay
     * approximately 16TRIADZ per year.
     * Here is the formula: 16.0 / (365 * 24) / 4096 = 1915
     * But I like the 1917 number better.
     */
    private static final TxnAmount FEE = new TxnAmount(1917L);

    /**
     * Max amount allowed amount in one txn
     */
    private static final TxnAmount MAX_PAYMENT = new TxnAmount(new BigDecimal("1"));

    /**
     * Text prefix for taxes
     */
    private static final String PREFIX = "TAXES";

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
        final List<SignedTransaction> txns = this.wallet.transactions();
        for (final SignedTransaction txn : txns) {
            final ParsedTxnData data = new ParsedTxnData(txn);
            final String[] details = data.details().split(" ");
            final String prefix = details[0];
            if (!prefix.equals(PREFIX) || details.length == 2) {
                final TriadaScore score = new TriadaScore(details[1]);
                if (scoreValid.test(score) || score.value() != EXACT_SCORE) {
                    if (score.strength() < this.strength && !this.ignoreScoreWeakness){

                    }
                }
            }
        }
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
