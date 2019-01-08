package io.triada.models.transaction;

import com.google.gson.JsonObject;
import io.triada.models.id.Id;
import io.triada.models.key.Key;

public interface Transaction {

    /**
     * @return Transaction in json format
     */
    JsonObject asJson();

    /**
     * @return Body of transaction
     */
    String body();

    SignedTransaction signed(Id<Long> id, Key prvtKey) throws Exception;


}
