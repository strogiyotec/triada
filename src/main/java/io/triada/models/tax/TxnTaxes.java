package io.triada.models.tax;

import com.google.common.collect.ImmutableMap;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.score.IsValidScore;
import io.triada.models.score.ReducesScore;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.wallet.Wallet;
import org.jooq.lambda.Unchecked;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.OffsetDateTime.parse;

/**
 * Tax transactions
 */
public final class TxnTaxes implements Tax {


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
    private static final TxnAmount MAX_PAYMENT = new TxnAmount(new BigDecimal("16"));

    /**
     * Text prefix for taxes
     */
    public static final String PREFIX = "TAXES";

    /**
     * Tolerate debt
     */
    public static final TxnAmount TRIAL = new TxnAmount(new BigDecimal("1"));

    /**
     * Wallet
     */
    private final Wallet wallet;

    /**
     * Should ignore score weakness
     */
    private final boolean ignoreScoreWeakness;

    /**
     * Current strength
     */
    private final int strength;

    public TxnTaxes(
            final Wallet wallet,
            final boolean ignoreScoreWeakness,
            final int strength
    ) {
        this.wallet = wallet;
        this.ignoreScoreWeakness = ignoreScoreWeakness;
        this.strength = strength;
    }

    public TxnTaxes(
            final Wallet wallet,
            final boolean ignoreScoreWeakness
    ) {
        this.wallet = wallet;
        this.ignoreScoreWeakness = ignoreScoreWeakness;
        this.strength = TriadaScore.STRENGTH;
    }

    public TxnTaxes(
            final Wallet wallet,
            final int strength
    ) {
        this.wallet = wallet;
        this.ignoreScoreWeakness = false;
        this.strength = strength;
    }

    public TxnTaxes(final Wallet wallet) {
        this.wallet = wallet;
        this.ignoreScoreWeakness = false;
        this.strength = TriadaScore.STRENGTH;
    }

    /**
     * @param details Details
     * @return True if this tax payment already exists in the wallet
     */
    @Override
    public boolean exists(final String details) {
        return this.wallet.transactions().stream().map(ParsedTxnData::new).anyMatch(data -> data.details().equals(details));
    }

    /**
     * @param rsaKey to sign
     * @param score  Score to pay
     * @return wallet with new payment
     * @throws Exception if failed
     */
    @Override
    public TxnTaxes pay(final RsaKey rsaKey, final Score score) throws Exception {
        final String[] invoice = score.invoice().split("@");
        final String prefix = invoice[0];
        final LongId id = new LongId(invoice[1]);

        return new TxnTaxes(this.wallet.substract(
                new TxnAmount(this.debt()),
                prefix,
                id,
                rsaKey,
                this.details(score)
        ));
    }

    /**
     * @return Sum of amounts in wallet
     */
    @Override
    public long paid() {
        final List<SignedTransaction> txns = this.wallet.transactions();
        long amount = 0;

        for (final SignedTransaction txn : txns) {
            final ParsedTxnData txnData = new ParsedTxnData(txn);
            final String[] details = txnData.details().split(" ", 2);
            if (TxnTaxes.isDetailsValid(details)) {
                final TriadaScore score = new TriadaScore(details[1]/*body*/);
                if (TxnTaxes.isScoreValid(score)) {
                    if (this.isStrengthValid(score)) {
                        if (txnData.amount().less(MAX_PAYMENT.value())) {
                            amount += txnData.amount().value();
                        }
                    }
                }
            }
        }
        return amount * -1;
    }

    /**
     * Debt of transaction , if less than 1 TRIADA can skip taxes
     *
     * @return Debt of wallet
     */
    @Override
    public long debt() {
        return FEE.value() * this.wallet.transactions().size() * this.wallet.age() - this.paid();
    }

    @Override
    public Optional<SignedTransaction> last() {
        final List<SignedTransaction> transactions = this.wallet.transactions();
        if (transactions.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(transactions.get(transactions.size() - 1));
        }
    }

    @Override
    public String details(final Score score) {
        return String.join(
                " ",
                PREFIX,
                new ReducesScore(EXACT_SCORE, score).asText()
        );
    }

    @Override
    public String asText() {
        return String.format(
                "A=%d hours,F=%d triada/th, T=%dt, Paid=%d",
                this.wallet.age(),
                FEE.value(),
                this.wallet.transactions().size(),
                Unchecked.supplier(this::debt).get()
        );
    }

    private boolean isStrengthValid(final Score score) {
        return score.strength() <= this.strength && !this.ignoreScoreWeakness;
    }

    private static boolean isScoreValid(final Score score) {
        return scoreValidator.test(score) && score.value() == EXACT_SCORE;
    }

    private static boolean isDetailsValid(final String[] details) {
        return details.length == 2 && details[0].equals(PREFIX);
    }
}
