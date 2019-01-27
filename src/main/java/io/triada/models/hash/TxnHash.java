package io.triada.models.hash;

public interface TxnHash {
    /**
     * @return Txn hash
     */
    String hash();

    /**
     * @return Txn nonce
     */
    String nonce();
}