package io.triada.models.transaction;

import io.triada.text.Text;

/**
 * Signature of transaction
 */
public interface SignedTransaction extends Text{

    /**
     * @return Origin transaction to be signed
     */
    Transaction origin();

    /**
     * @return Signature of transaction
     */
    String signature();
}
