package io.triada.models.transaction;

import com.google.gson.JsonObject;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;

import java.util.Date;
import java.util.Objects;

/**
 * Parse data from txn
 */
public final class ParsedTxnData implements TriadaTxn.Data {
    private final int id;
    private final Date date;
    private final TxnAmount txnAmount;
    private final String prefix;
    private final String details;
    private final SignedTransaction signed;
    private final LongId bnf;

    public ParsedTxnData(final SignedTransaction signed) {
        final JsonObject jsonObject = signed.origin().asJson();

        this.id = jsonObject.get("id").getAsInt();
        this.date = new Date(jsonObject.get("date").getAsLong());
        this.txnAmount = new TxnAmount(jsonObject.get("amount").getAsLong());
        this.prefix = jsonObject.get("prefix").getAsString();
        this.details = jsonObject.get("details").getAsString();
        this.signed = signed;
        this.bnf = new LongId(jsonObject.get("bnf").getAsLong());
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
    public LongId bnf() {
        return this.bnf;
    }

    @Override
    public String details() {
        return this.details;
    }

    @Override
    public String asText() {
        return String.join(
                ";",
                this.signed.origin().body(),
                this.signed.signature()

        );
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TriadaTxn.Data)) {
            return false;
        }
        final ParsedTxnData data = (ParsedTxnData) obj;
        return Objects.equals(this.id, data.id) &&
                Objects.equals(this.bnf.id(), data.bnf.id()) &&
                Objects.equals(this.date, data.date) &&
                Objects.equals(this.details, data.prefix) &&
                Objects.equals(this.details, data.details) &&
                Objects.equals(this.txnAmount.asText(2), data.txnAmount.asText(2));
    }
}
