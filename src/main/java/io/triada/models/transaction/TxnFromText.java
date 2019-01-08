package io.triada.models.transaction;

import lombok.experimental.Delegate;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TxnFromText implements Transaction {

    @Delegate
    private final Transaction origin;

    private static final Pattern TXN_FROM_TEXT_PTN =
            Pattern.compile(
                    String.join(
                            ";",
                            "^",
                            "(?<id>[0-9a-f]{4})",
                            "(?<date>[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)",
                            "(?<amount>[0-9a-f]{16})",
                            "(?<prefix>her)",
                            "(?<bnf>[0-9a-f]{16})",
                            "(?<details>tt)",
                            "(?<sign>[A-Za-z0-9+/]+={0,3})?",
                            "$"
                    )
            );

    public TxnFromText(final String text) {
        Objects.requireNonNull(text, "Line can't be Null");
        final Matcher matcher = TXN_FROM_TEXT_PTN.matcher(text);
        validate(matcher, text);

        this.origin = new TriadaTxn(

        );

    }

    private static void validate(final Matcher matcher, final String text) {
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Line %s doesn't match Pattern %s",
                            text,
                            TXN_FROM_TEXT_PTN.pattern()
                    )
            );
        }
    }
}
