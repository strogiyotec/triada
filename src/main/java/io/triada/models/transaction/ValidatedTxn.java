package io.triada.models.transaction;

import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import lombok.experimental.Delegate;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Create instance of transaction that was validated
 */
//TODO: change type of txnamount to amount
public final class ValidatedTxn implements Transaction {

    /**
     * Pattern to check PREFIX
     */
    private static final Pattern PREFIX_P = Pattern.compile("^[a-zA-Z0-9/+]+$");

    /**
     * Pattern to check DETAILS
     */
    private static final Pattern DETAILS_P = Pattern.compile("^[a-zA-Z0-9 @!?*_\\-.:,'/]+$");

    /**
     * Delegate transaction that was validated
     */
    @Delegate
    private final Transaction origin;

    public ValidatedTxn(final String id,
                        final Date date,
                        final TxnAmount amount,
                        final String prefix,
                        final LongId bnf,
                        final String details) {
        ValidatedTxn.validate(id, date, amount, bnf, details, prefix);
        this.origin = new TriadaTxn(
                Integer.parseInt(id, 16),
                date,
                amount,
                prefix,
                bnf,
                details
        );
    }

    public ValidatedTxn(final String id,
                        final TxnAmount amount,
                        final String prefix,
                        final LongId bnf,
                        final String details) {
        this(
                id,
                new Date(),
                amount,
                prefix,
                bnf,
                details
        );
    }

    /**
     * Validate data to create transaction
     */
    private static void validate(final String id,
                                 final Date date,
                                 final TxnAmount amount,
                                 final LongId bnf,
                                 final String details,
                                 final String prefix) {
        ValidatedTxn.validateId(id);
        ValidatedTxn.validateDate(date);
        ValidatedTxn.validateAmount(amount);
        ValidatedTxn.validatePrefix(prefix);
        ValidatedTxn.validateBnf(bnf);
        ValidatedTxn.validateDetails(details);

    }

    /**
     * Id should be int
     *
     * @param id Txn id
     */
    private static void validateId(final String id) {
        final boolean matches = id.matches("\\p{XDigit}+");
        if (!matches) {
            throw new IllegalArgumentException(
                    String.format(
                            "Id %s should be hex",
                            id
                    )
            );
        }
    }

    /**
     * Date can't be null and represent future time
     *
     * @param date Txn Date
     */
    private static void validateDate(final Date date) {
        requireNonNull(date, "The time can't bu NULL");
        if (date.compareTo(new Date()) > 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Time can't in future %s", date.toString()));
        }
    }

    /**
     * Amount can't be null and be zero
     *
     * @param amount Txn amount
     */
    private static void validateAmount(final TxnAmount amount) {
        requireNonNull(amount, "Amount cant bu NULL");
        if (amount.zero()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Amount can't be zero : %d",
                            amount.value()
                    )
            );
        }
    }

    /**
     * Details can't me empty
     *
     * @param details Txn details
     */
    private static void validateDetails(final String details) {
        if (StringUtils.isEmpty(details)) {
            throw new IllegalArgumentException("Details can't be empty");
        }
        final int length = details.length();
        if (length > 512) {
            throw new IllegalArgumentException(
                    String.format(
                            "Details is too long %d",
                            length
                    )
            );
        }
        if (!DETAILS_P.matcher(details).find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Details doesn't match pattern :'%s' %s ",
                            details, DETAILS_P.pattern()
                    )
            );
        }

    }

    /**
     * Prefix can't be null
     * Length >8 and < 32
     *
     * @param prefix Txn prefix
     */
    private static void validatePrefix(final String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            throw new IllegalArgumentException("Prefix can't be empty");
        }
        final int length = prefix.length();
        if (length < 8) {
            throw new IllegalArgumentException(
                    String.format(
                            "Prefix length is too short : %d",
                            length
                    )
            );
        }
        if (length > 32) {
            throw new IllegalArgumentException(
                    String.format(
                            "Prefix length is too long : %d",
                            length
                    )
            );
        }
        if (!PREFIX_P.matcher(prefix).find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Prefix doesn't match pattern :'%s' %s ",
                            prefix, PREFIX_P.pattern()
                    )
            );
        }

    }

    /**
     * Bnf can't be null
     *
     * @param bnf Receiver id
     */
    private static void validateBnf(final LongId bnf) {
        requireNonNull(bnf, "Bnf can't be null");
    }

}
