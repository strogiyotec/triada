package io.triada.models.transaction;

import com.google.gson.JsonObject;

public interface Transaction {

    JsonObject asJson();

    String body();

    String signature();

}
