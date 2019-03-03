package io.triada.models.wallet;

import io.triada.models.amount.Amount;
import io.triada.models.amount.TxnAmount;
import io.triada.models.head.Head;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.transaction.SignedTransaction;
import io.triada.text.Text;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface Wallet extends Text {

    /**
     * @return Age of wallet in hours
     */
    long age();

    /**
     * @return Wallet file
     */
    File file();

    /**
     * @return Head of wallet
     */
    Head head();

    /**
     * @return Balance of the wallet
     */
    Amount<Long> balance();

    /**
     * @param id  txn id
     * @param bnf Beneficiary id
     * @return Returns TRUE if the wallet contains a payment received with the specified
     * which was sent by the specified beneficiary.
     */
    boolean exists(int id, LongId bnf);

    /**
     * @param prefix Prefix
     * @return TRUE if the public key of the wallet includes this payment
     * prefix of the invoice.
     */
    boolean prefix(String prefix);

    /**
     * @param transaction New Transaction
     * @return Wallet with new Transaction
     * @throws Exception if Failed
     */
    Wallet add(SignedTransaction transaction) throws Exception;

    /**
     * @param amount  To sub
     * @param prefix  Of txn
     * @param id      is the wallet ID of the paying or receiving wallet
     * @param pvt     Private key to sign txn
     * @param details Details of transaction
     * @return Wallet with subtracted transaction
     * @throws Exception if Failed
     */
    Wallet substract(TxnAmount amount, String prefix, LongId id, RsaKey pvt, String details, Date date) throws Exception;

    /**
     * @return List of txns
     */
    List<SignedTransaction> transactions();

    @Override
    boolean equals(Object other);

    default Wallet substract(TxnAmount amount, String prefix, LongId id, RsaKey pvt, String details) throws Exception {
        return this.substract(amount, prefix, id, pvt, details, new Date());
    }
}
