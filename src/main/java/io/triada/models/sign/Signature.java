package io.triada.models.sign;

import io.triada.models.id.Id;
import io.triada.models.key.Key;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.Transaction;

public interface Signature {

    /**
     * @param privateKey  Private RSA key
     * @param id          wallet id
     * @param transaction The transaction
     * @return Sign and return sign
     * @throws Exception if failed
     */
    String sign(Key privateKey, Id<Long> id, Transaction transaction) throws Exception;

    /**
     * @param publicKey   Public key of wallet
     * @param id          Paying wallet id
     * @param transaction to validate
     * @return true if transaction is valid
     * @throws Exception if failed
     */
    boolean valid(Key publicKey, Id<Long> id, SignedTransaction transaction) throws Exception;
}
