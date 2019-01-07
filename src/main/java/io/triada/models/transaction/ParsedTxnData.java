package io.triada.models.transaction;

import com.google.gson.JsonObject;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.WalletId;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * Parse data from txn
 */
@AllArgsConstructor
public final class ParsedTxnData implements TriadaTxn.Data {
    private final int id;
    private final Date date;
    private final TxnAmount txnAmount;
    private final String prefix;
    private final String details;

    public ParsedTxnData(final Transaction txn) {
        final JsonObject jsonObject = txn.asJson();
        this.id = jsonObject.get("id").getAsInt();
        this.date = new Date(jsonObject.get("date").getAsLong());
        this.txnAmount = new TxnAmount(jsonObject.get("amount").getAsLong());
        this.prefix = jsonObject.get("prefix").getAsString();
        this.details = jsonObject.get("details").getAsString();
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public Date date() {
        return this.date;
    }

    @Override
    public TxnAmount amount() {
        return this.txnAmount;
    }

    @Override
    public String prefix() {
        return this.prefix;
    }

    @Override
    public WalletId bnf() {
        return null;
    }

    @Override
    public String details() {
        return this.details;
    }
}
