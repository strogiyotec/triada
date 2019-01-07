package io.triada.models.transaction;

import io.triada.models.amount.TxnAmount;
import io.triada.models.id.WalletId;
import lombok.experimental.Delegate;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public final class ValidatedTxn implements Transaction {

    private static final Pattern PREFIX_P = Pattern.compile("^[a-zA-Z0-9]+$");

    private static final Pattern DETAILS_P = Pattern.compile("^[a-zA-Z0-9 @!?*_\\-.:,'/]+$");

    @Delegate
    private final Transaction origin;

    public ValidatedTxn(final int id,
                        final Date date,
                        final TxnAmount amount,
                        final String prefix,
                        final WalletId bnf,
                        final String details) {
        ValidatedTxn.vaildate(id, date, amount, bnf, details, prefix);
        this.origin = new TriadaTxn(
                id,
                date,
                amount,
                prefix,
                bnf,
                details
        );
    }

    private static void vaildate(final int id,
                                 final Date date,
                                 final TxnAmount amount,
                                 final WalletId bnf,
                                 final String details,
                                 final String prefix) {
        ValidatedTxn.validateId(id);
        ValidatedTxn.validateDate(date);
        ValidatedTxn.validateAmount(amount);
        ValidatedTxn.validatePrefix(prefix);
        ValidatedTxn.validatePrefix(prefix);
        ValidatedTxn.validateBnf(bnf);
        ValidatedTxn.validateDetails(details);

    }

    private static void validateId(final int id) {
        if (id < 0) {
            throw new IllegalArgumentException(String.format(
                    "The ID of transaction can't be negative :%d",
                    id
            ));
        }
    }

    private static void validateDate(final Date date) {
        requireNonNull(date, "The time can't bu NULL");
        if (date.compareTo(new Date()) > 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Time can't in future %s", date.toString()));
        }
    }

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

    private static void validateBnf(final WalletId bnf) {
        requireNonNull(bnf, "Bnf can't be null");
    }

}
