package io.triada.models.transaction;

import com.google.gson.JsonObject;
import io.triada.models.id.Id;
import io.triada.models.key.Key;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class TxnEnvelope implements Transaction{
    private final Transaction origin;

    @Override
    public final JsonObject asJson() {
        return this.origin.asJson();
    }

    @Override
    public final String body() {
        return this.origin.body();
    }

    @Override
    public final SignedTransaction signed(final Id<Long> id, final Key prvtKey) throws Exception {
        return this.origin.signed(id, prvtKey);
    }
}
