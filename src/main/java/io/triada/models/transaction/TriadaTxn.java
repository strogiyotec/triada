package io.triada.models.transaction;

import com.google.gson.JsonObject;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.WalletId;

import java.util.Date;


final class TriadaTxn implements Transaction {

    public TriadaTxn(final int id,
                     final Date date,
                     final TxnAmount amount,
                     final String prefix,
                     final WalletId bnf,
                     final String details) {

    }

    @Override
    public JsonObject asJson() {
        return null;
    }

    @Override
    public String body() {
        return null;
    }

    @Override
    public String signature() {
        return null;
    }

    public interface Data {
        int id();

        Date date();

        TxnAmount amount();

        String prefix();

        WalletId bnf();

        String details();
    }
}
