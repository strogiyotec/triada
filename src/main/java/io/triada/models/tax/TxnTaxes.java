package io.triada.models.tax;

import com.google.common.collect.ImmutableMap;
import io.triada.models.amount.TxnAmount;
import io.triada.models.score.IsValidScore;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.wallet.Wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.time.OffsetDateTime.parse;

public final class TransactionTaxes implements Tax {


    /**
     * The exact score a wallet must but in order to pay taxes
     */
    private static final int EXACT_SCORE = 8;

    /**
     * Score validator
     */
    private static final IsValidScore scoreValidator = new IsValidScore();

    /**
     * When score strengths were updated. The numbers here indicate the
     * strengths we accepted before these dates.
     */
    private static final Map<Date, Integer> MILESTONES = ImmutableMap.of(
            Date.from(parse("2018-11-30T00:00:00Z").toInstant()), 6,
            Date.from(parse("2018-12-09T00:00:00Z").toInstant()), 7
    );

    /**
     * This is how much we charge per one transaction per hour
     * of storage. A wallet of 4096 transactions will pay
     * approximately 16TRIADZ per year.
     * Here is the formula: 16.0 / (365 * 24) / 4096 = 1915
     * But I like the 1917 number better.
     */
    public static final TxnAmount FEE = new TxnAmount(1917L);

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

    public TransactionTaxes(final Wallet wallet) {
        this.wallet = wallet;
        this.ignoreScoreWeakness = false;
        this.strength = TriadaScore.STRENGTH;
    }

    @Override
    public void pay() throws Exception {

    }

    @Override
    public int paid() throws Exception {
        final List<SignedTransaction> txns = this.wallet.transactions();
        final List<SignedTransaction> scored = new ArrayList<>(10);

        for (final SignedTransaction txn : txns) {
            final ParsedTxnData txnData = new ParsedTxnData(txn);
            final String[] details = txnData.details().split(" ");
            final String prefix = details[0];
            if (prefix.equals(PREFIX) && details.length == 2) {
                final TriadaScore score = new TriadaScore(details[1]);
                if (scoreValid(score)) {
                    if (score.strength() < this.strength && !this.ignoreScoreWeakness) {
                        MILESTONES.forEach((date, strength) -> {
                            if (txnData.date().compareTo(date) < 0 && score.strength() >= strength && txnData.amount().less(MAX_PAYMENT.value())) {
                                scored.add(txn);
                            }
                        });
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public long debt() throws Exception {
        return FEE.value() * this.wallet.transactions().size() * this.wallet.age() - this.paid();
    }

    @Override
    public String asText() {
        return String.format(
                "A=%d hours,F=%dz/th, T="
        );
    }

    private boolean strengthValid(final Score score) {

    }

    private static boolean scoreValid(final Score score) {
        return scoreValidator.test(score) && score.value() == EXACT_SCORE;
    }
}
