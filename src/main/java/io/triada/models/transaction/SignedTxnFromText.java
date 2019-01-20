package io.triada.models.transaction;

import io.triada.models.amount.TxnAmount;
import io.triada.models.id.WalletId;

import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Signed txn from file
 */
public final class SignedTxnFromText implements SignedTransaction {

    /**
     * Txn from line
     */
    private final Transaction txn;

    /**
     * Signature from line
     */
    private final String signature;

    /**
     * Pattern to parse txn
     */
    private static final Pattern TXN_FROM_TEXT_PTN =
            Pattern.compile(
                    String.join(
                            ";",
                            "(?<id>[0-9a-f]{4})",
                            "(?<date>[0-9]{10,12})",
                            "(?<amount>[0-9a-f]{16})",
                            "(?<prefix>[a-zA-Z0-9]+)",
                            "(?<bnf>[0-9a-f]{16})",
                            "(?<details>[a-zA-Z0-9 @\\!\\?\\*_\\-\\.:,\\'/]+)",
                            "(?<sign>[A-Za-z0-9+/]+={0,3})?"
                    )
            );

    public SignedTxnFromText(final String text) {
        Objects.requireNonNull(text, "Line can't be Null");
        final Matcher matcher = TXN_FROM_TEXT_PTN.matcher(text);
        validate(matcher, text);

        this.txn = new TriadaTxn(
                Integer.parseInt(matcher.group(1),16),
                new Date(Long.valueOf(matcher.group(2))),
                new TxnAmount(matcher.group(3)),
                matcher.group(4),
                new WalletId(matcher.group(5)),
                matcher.group(6)
        );
        this.signature = matcher.group(7);

    }

    /**
     * @param matcher Of text
     * @param text    Line to parse
     */
    private static void validate(final Matcher matcher, final String text) {
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Line '%s' doesn't match Pattern %s",
                            text,
                            TXN_FROM_TEXT_PTN.pattern()
                    )
            );
        }
    }

    @Override
    public Transaction origin() {
        return this.txn;
    }

    @Override
    public String signature() {
        return this.signature;
    }

    @Override
    public String asText() {
        return new ParsedTxnData(this).asText();
    }
}
