package io.triada.models.wallet;

import io.triada.models.amount.Amount;
import io.triada.models.key.Key;
import io.triada.models.transaction.Transaction;
import io.triada.text.Text;

public interface Wallet extends Text {
    Key walletKey();

    String mnemo();

    Amount<Long> balance();

    Wallet add(Transaction transaction) throws Exception;
}
