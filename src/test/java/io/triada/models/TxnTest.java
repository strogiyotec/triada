package io.triada.models;

import com.google.gson.JsonObject;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.WalletId;
import io.triada.models.transaction.ValidatedTxn;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;

public final class TxnTest extends Assert {

    private final Date now = new Date();
    private final String id = "fffffc6f00000000";
    private final String prefix = "NOPREFIX";
    private final WalletId walletId = new WalletId();
    private final String desc = "Test to see json";
    private final TxnAmount txnAmount = new TxnAmount(1000L);

    @Test
    public void testJson() {
        final ValidatedTxn validatedTxn = new ValidatedTxn(
                id,
                now,
                txnAmount,
                prefix,
                walletId,
                desc
        );
        final JsonObject jsonObject = validatedTxn.asJson();
        assertThat(
                jsonObject.get("id").getAsInt(),
                is(id)
        );
        assertThat(
                jsonObject.get("date").getAsLong(),
                is(now.getTime())
        );
        assertThat(
                jsonObject.get("amount").getAsLong(),
                is(txnAmount.value())
        );
        assertThat(
                jsonObject.get("prefix").getAsString(),
                is(prefix)
        );
        assertThat(
                jsonObject.get("details").getAsString(),
                is(desc)
        );

    }

    public void test(){
        final ValidatedTxn validatedTxn = new ValidatedTxn(
                id,
                now,
                txnAmount,
                prefix,
                walletId,
                desc
        );
    }
}
