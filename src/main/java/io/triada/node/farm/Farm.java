package io.triada.node.farm;

import io.triada.text.Jsonable;
import io.triada.text.Text;

public interface Farm extends Text, Jsonable {
    void start() throws Exception;

    void cleanUp() throws Exception;

    void save();

    void load();
}
