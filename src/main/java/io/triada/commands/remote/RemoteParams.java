package io.triada.commands.remote;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Params for remote command
 */
@AllArgsConstructor
final class RemoteParams {

    /**
     * Params
     */
    private final List<String> params;

    /**
     * Show all registered nodes
     *
     * @return Show
     */
    boolean show() {
        return this.params.stream().anyMatch(p -> p.contains("show"));
    }

    /**
     * Remove all registered remote nodes
     *
     * @return Clean
     */
    boolean clean() {
        return this.params.stream().anyMatch(p -> p.contains("clean"));
    }

    /**
     * Remove single node
     *
     * @return Remove
     */
    boolean remove() {
        return this.params.stream().anyMatch(p -> p.contains("remove"));
    }

    /**
     * Restore it back to the default list of remotes
     *
     * @return Reset
     */
    boolean reset() {
        return this.params.stream().anyMatch(p -> p.contains("reset"));
    }

    /**
     * @param host Host
     * @param port Port
     * @return True if need to ignore given host with port
     */
    boolean ignore(String host, int port) {
        return this.params.stream()
                .filter(p -> p.contains("ignore-node"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .map(p -> Arrays.asList(p.split(",")))
                .flatMap(Collection::stream)
                .anyMatch(p -> {
                    final String[] hostAndPort = p.split(":");
                    return host.equals(hostAndPort[0]) && port == Integer.parseInt(hostAndPort[1]);
                });
    }

    /**
     * Add all master nodes to the list
     *
     * @return Masters
     */
    boolean masters() {
        return this.params.stream().anyMatch(p -> p.contains("masters"));
    }

    /**
     * Add new node to the list
     *
     * @return Masters
     */
    boolean add() {
        return this.params.stream().anyMatch(p -> p.contains("add"));
    }

    /**
     * Skip ping on add
     *
     * @return Skip ping
     */
    boolean skipPing() {
        return this.params.stream().anyMatch(p -> p.contains("skip-ping"));
    }

    int port() {
        return this.params.stream()
                .filter(p -> p.contains("port"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(RemoteNodes.PORT);
    }

    String host() {
        return this.params.stream()
                .filter(p -> p.contains("host"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Add host"));
    }
}
