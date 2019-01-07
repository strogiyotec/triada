package io.triada.models.sign;

import io.triada.models.id.Id;
import io.triada.models.key.Key;
import io.triada.models.transaction.Transaction;

public final class TriadaSignTxn implements SignTxn {
    @Override
    public String sign(final Key privateKey,
                       final Id<String> id,
                       final Transaction transaction
    ) throws Exception {
        return privateKey.sign(TriadaSignTxn.body(transaction, id.id()));
    }

    //TODO:add network check
    @Override
    public boolean valid(final Key publicKey,
                         final Id<String> id,
                         final Transaction transaction
    ) throws Exception {
        return publicKey.verify(transaction.signature(), TriadaSignTxn.body(transaction, id.id()));
    }


    /**
     * @return Body of transaction signature
     */
    private static String body(final Transaction transaction, final String id) throws Exception {
        return String.join(" ", transaction.body(), id);
    }

}
