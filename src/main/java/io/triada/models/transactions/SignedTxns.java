package io.triada.models.transactions;

import io.triada.models.transaction.SignedTransaction;

import java.util.List;

public interface SignedTxns<T extends SignedTransaction> {
    List<T> txns();

    String asText();
}
