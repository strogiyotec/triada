package io.triada.models.transaction;

/**
 * Signature of transaction
 */
public interface SignedTransaction {

    /**
     * @return Origin transaction to be signed
     */
    Transaction origin();

    /**
     * @return Signature of transaction
     */
    String signature();
}
