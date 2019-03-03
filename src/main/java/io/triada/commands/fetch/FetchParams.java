package io.triada.commands.fetch;

import io.triada.Triada;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
final class FetchParams {

    private final List<String> params;

    /**
     * @return tolerateQuorum number from this.params
     */
    public int tolerateQuorum() {
        return this.params.stream()
                .filter(param -> param.contains("tolerate-quorum"))
                .findFirst()
                .map(quorum -> quorum.substring(quorum.indexOf("=") + 1))
                .map(Integer::parseInt)
                .orElse(0);
    }

    /**
     * @return List of ignore nodes which are separated by ','
     */
    public List<String> ignoreNodes() {
        return this.params.stream().filter(p -> p.contains("ignore-node"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .map(p -> Arrays.asList(p.split(",")))
                .findFirst()
                .orElse(Collections.emptyList());
    }

    /**
     * @return network name
     */
    public String network() {
        return this.params.stream()
                .filter(p -> p.contains("network"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElse(Triada.TEST_NETWORK);
    }

    /**
     * @return wallet id if present or orElse if id is not present
     */
    public List<String> wallets(final List<String> orElse) {
        return this.params.stream()
                .filter(p -> p.contains("wallet="))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .map(p -> Arrays.asList(p.split(",")))
                .orElse(orElse);
    }

    public boolean tolerateEdges() {
        return this.params.contains("tolerate-edges");
    }

    public boolean ignoreScoreWeakness() {
        return this.params.contains("ignore-score-weakness");
    }

}
