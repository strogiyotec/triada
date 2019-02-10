package io.triada.node.farm.node;

import io.triada.text.Text;

public interface NodeData extends Text{
    String host();

    int port();

    int errors();

    int score();

    boolean master();

}
