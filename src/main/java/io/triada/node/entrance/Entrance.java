package io.triada.node.entrance;

import io.triada.functions.VoidYield;
import io.triada.models.wallet.Copies;
import io.triada.text.Jsonable;

import java.io.File;
import java.util.List;

public interface Entrance extends Jsonable {

    void start(VoidYield yield) throws Exception;

    List<String> push(String id, String body) throws Exception;

    List<String> merge(String id, Copies<File> copies) throws Exception;
}
