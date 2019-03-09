package io.triada.text;

import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.SignedTxnFromText;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Calculate max txn id , if no txns return 0 in hex , otherwise
 * find last txn id increment it and return in hex
 */
@UtilityClass
public final class NextTxnId {

    /**
     * The first transaction id
     */
    private static final String FIRST_TXN_ID = new HexNumber(4, 1).asText();

    public String next(final List<SignedTransaction> transactions) {
        if (transactions.isEmpty()) {
            return FIRST_TXN_ID;
        } else {
            final int id = new ParsedTxnData(transactions.get(transactions.size() - 1)).id();
            return new HexNumber(4, id + 1).asText();

        }
    }
}
