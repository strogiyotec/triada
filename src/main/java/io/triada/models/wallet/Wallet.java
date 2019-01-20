package io.triada.models.wallet;

import io.triada.models.amount.Amount;
import io.triada.models.key.Key;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.Transaction;
import io.triada.text.Text;

import java.util.List;

public interface Wallet extends Text {
    Key walletKey();

    String mnemo();

    Amount<Long> balance();

    Wallet add(Transaction transaction) throws Exception;

    Wallet substract(Transaction transaction);

    List<SignedTransaction> transactions();
}
