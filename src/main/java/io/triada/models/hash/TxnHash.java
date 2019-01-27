package io.triada.models.hash;

import java.math.BigInteger;

public interface TxnHash {
    /**
     * @return Txn hash
     */
    String hash();

    /**
     * @return Txn nonce
     */
    BigInteger nonce();
}