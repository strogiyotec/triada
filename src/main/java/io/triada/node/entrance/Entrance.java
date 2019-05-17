package io.triada.node.entrance;

import io.triada.functions.VoidYield;
import io.triada.models.wallet.Copies;
import io.triada.text.Jsonable;

import java.util.Collections;
import java.util.List;

public interface Entrance extends Jsonable {

    void start(VoidYield yield) throws Exception;

    List<String> push(String id, String body, List<String> params) throws Exception;

    List<String> merge(String id, Copies copies) throws Exception;

    default List<String> push(String id, String body) throws Exception {
        return this.push(id, body, Collections.emptyList());
    }
}
