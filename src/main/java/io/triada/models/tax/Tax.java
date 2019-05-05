package io.triada.models.tax;

import io.triada.models.key.RsaKey;
import io.triada.models.score.Score;
import io.triada.models.transaction.SignedTransaction;
import io.triada.text.Text;

import java.util.Optional;

/**
 * Taxes of transaction
 */
public interface Tax extends Text {

    /**
     * @param details Transaction details
     * @return True if transaction tax exists with given details
     */
    boolean exists(String details);

    /**
     * @param rsaKey RsaKey to sign transaction
     * @param score  Score of node to pay
     * @return new Tax instance
     * @throws Exception if failed
     *                   Pay taxes to node with given score
     */
    Tax pay(RsaKey rsaKey, Score score) throws Exception;

    /**
     * @return Amount of taxes to be paid
     */
    long paid();

    /**
     * @return Current  Taxes debt
     */
    long debt();

    /**
     * @return Last Tax payment
     */
    Optional<SignedTransaction> last();

    /**
     * @param score Score of remote node
     * @return Details of transaction to given score
     */
    String details(Score score);

    /**
     * @return True if wallet in dept
     */
    boolean inDept();

}