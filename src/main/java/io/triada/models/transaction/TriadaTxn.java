package io.triada.models.transaction;

import com.google.gson.JsonObject;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.Id;
import io.triada.models.id.WalletId;
import io.triada.models.key.Key;
import io.triada.text.Text;
import org.jooq.lambda.Unchecked;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


final class TriadaTxn implements Transaction {

    /**
     * Always return the same signature
     */
    private final ConcurrentMap<Integer, SignedTransaction> signedTxnCache = new ConcurrentHashMap<>(2, 1.0F, 2);
    /**
     * Id of transaction
     */
    private final int id;

    /**
     * Date of transaction
     */
    private final Date date;

    /**
     * Amount of transaction
     */
    private final TxnAmount amount;

    /**
     * Prefix from invoice
     */
    private final String prefix;

    /**
     * The paying or receiving wallet
     */
    private final WalletId bnf;

    /**
     * Details of transaction
     */
    private final String details;


    public TriadaTxn(final int id, final Date date, final TxnAmount amount, final String prefix, final WalletId bnf, final String details) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.prefix = prefix;
        this.bnf = bnf;
        this.details = details;
    }

    @Override
    public JsonObject asJson() {
        final JsonObject txnJsn = new JsonObject();

        txnJsn.addProperty("id", this.id);
        txnJsn.addProperty("date", this.date.getTime());
        txnJsn.addProperty("amount", this.amount.value());
        txnJsn.addProperty("prefix", this.prefix);
        txnJsn.addProperty("bnf", this.bnf.id());
        txnJsn.addProperty("details", this.details);

        return txnJsn;
    }

    @Override
    public String body() {
        return String.join(
                " ",
                String.valueOf(this.id),
                String.valueOf(this.date.getTime()),
                String.valueOf(this.amount.value()),
                this.prefix,
                String.valueOf(this.bnf.id()),
                this.details
        );
    }

    @Override
    public SignedTransaction signed(final Id<Long> id, final Key prvtKey) throws Exception {
        final Transaction txn = this;//because Unchecked.function doesn't recognized this
        return this.signedTxnCache.computeIfAbsent(
                System.identityHashCode(this),
                Unchecked.function(
                        hash -> new SignedTriadaTxn(
                                txn,
                                prvtKey,
                                id
                        )
                ));
    }

    public interface Data extends Text {
        int id();

        Date date();

        TxnAmount amount();

        String prefix();

        WalletId bnf();

        String details();
    }
}
