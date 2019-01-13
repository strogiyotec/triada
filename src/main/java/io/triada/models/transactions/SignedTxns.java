package io.triada.models.transactions;

import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.Transaction;

import java.util.List;

public interface SignedTxns<T extends SignedTransaction> {
    List<T> txns();

    String asText();

    SignedTxns<T> add(SignedTransaction txn) throws Exception;
}
