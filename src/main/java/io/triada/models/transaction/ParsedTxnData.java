package io.triada.models.transaction;

import com.google.gson.JsonObject;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;

import java.util.Date;

/**
 * Parse data from txn
 */
public final class ParsedTxnData implements TriadaTxn.Data {
    private final String id;
    private final Date date;
    private final TxnAmount txnAmount;
    private final String prefix;
    private final String details;
    private final SignedTransaction signed;

    public ParsedTxnData(final SignedTransaction signed) {

        final JsonObject jsonObject = signed.origin().asJson();
        this.id = jsonObject.get("id").getAsString();
        this.date = new Date(jsonObject.get("date").getAsLong());
        this.txnAmount = new TxnAmount(jsonObject.get("amount").getAsLong());
        this.prefix = jsonObject.get("prefix").getAsString();
        this.details = jsonObject.get("details").getAsString();
        this.signed = signed;
    }

    @Override
    public String id() {
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
    public LongId bnf() {
        return null;
    }

    @Override
    public String details() {
        return this.details;
    }

    @Override
    public String asText() {
        return String.join(
                ";",
                String.valueOf(this.id),
                String.valueOf(this.date.getTime()),
                String.valueOf(this.txnAmount.value()),
                this.prefix,
                String.valueOf(this.bnf().id()),
                this.signed.signature()

        );
    }
}
