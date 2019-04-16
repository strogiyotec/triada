package io.triada.node;

import io.triada.text.Text;

import java.util.List;

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

    static boolean has(final List<NodeData> nodes, final String host, final int port) {
        return nodes.stream().anyMatch(node -> node.host().equals(host) && node.port() == port);
    }
}
