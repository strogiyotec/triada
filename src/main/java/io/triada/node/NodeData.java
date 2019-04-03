package io.triada.node;

import io.triada.text.Text;

public interface NodeData extends Text {

    /**
     * @return Node host
     */
    String host();

    /**
     * @return Node port
     */
    int port();

    /**
     * @return Errors of given node
     */
    int errors();

    /**
     * @return Node's score
     */
    int score();

    /**
     * @return Node is master
     */
    boolean master();

    /**
     * @param host   Host
     * @param port   Port
     * @param errors Errors
     * @param score  Score
     * @return Text representation
     */
    String asText(String host, int port, int errors, int score);

}
