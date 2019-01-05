package io.triada.models.id;

import io.triada.models.Id;
import io.triada.text.HexText;
import lombok.EqualsAndHashCode;

import java.util.regex.Pattern;

/**
 * Wallet id
 */
@EqualsAndHashCode(of = "id")
public final class WalletId implements Id<Long> {

    /**
     * Id pattern
     */
    private static final Pattern ID_PTN = Pattern.compile("^[0-9a-fA-F]{16}$");

    /**
     * Id
     */
    private final Long id;

    public WalletId(final String id) {
        validate(id);
        this.id = Long.valueOf(id, 16);
    }


    @Override
    public Long id() {
        return this.id;
    }

    @Override
    public String toString() {
        return new HexText(id).toString();
    }

    /**
     * Validate given id
     *
     * @param id to validate
     */
    private static void validate(final String id) {
        if (!ID_PTN.matcher(id).find()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid wallet id %s",
                            id
                    )
            );
        }
    }
}
