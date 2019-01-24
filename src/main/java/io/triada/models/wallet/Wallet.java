package io.triada.models.wallet;

import io.triada.models.amount.Amount;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.Key;
import io.triada.models.key.RsaKey;
import io.triada.models.transaction.SignedTransaction;
import io.triada.text.Text;

import java.util.List;

public interface Wallet extends Text {
    Key walletKey();

    String mnemo();

    Amount<Long> balance();

    Wallet add(SignedTransaction transaction) throws Exception;

    Wallet substract(TxnAmount amount, String prefix, LongId id, RsaKey pvt, String details) throws Exception;

    List<SignedTransaction> transactions();
}
