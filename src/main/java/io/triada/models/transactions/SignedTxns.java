package io.triada.models.transactions;

import io.triada.models.transaction.SignedTransaction;
import io.triada.text.Text;

import java.util.List;

public interface SignedTxns extends Text {
    /**
     * @return List of txns
     */
    List<SignedTransaction> txns();


    /**
     * @param txn New transaction
     * @return new instance of {@link SignedTxns} with new txn
     * @throws Exception if failed
     */
    SignedTxns add(SignedTransaction txn) throws Exception;
}
