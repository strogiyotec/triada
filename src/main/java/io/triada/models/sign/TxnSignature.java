package io.triada.models.sign;

import io.triada.models.id.Id;
import io.triada.models.key.Key;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.Transaction;

public final class TxnSignature implements Signature {
    @Override
    public String sign(final Key privateKey,
                       final Id<Long> id,
                       final Transaction transaction
    ) throws Exception {
        return privateKey.sign(TxnSignature.body(transaction, id.id()));
    }

    //TODO:add network check
    @Override
    public boolean valid(final Key publicKey,
                         final Id<Long> id,
                         final SignedTransaction signedTransaction
    ) throws Exception {
        return publicKey.verify(signedTransaction.signature(), TxnSignature.body(signedTransaction.origin(), id.id()));
    }


    /**
     * @return Body of transaction sign
     */
    private static String body(final Transaction transaction, final Long id) throws Exception {
        return String.join(" ", transaction.body(), id.toString());
    }

}
