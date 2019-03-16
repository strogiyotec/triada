package io.triada.commands.taxes;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
final class TaxesParams {

    private final List<String> params;

    boolean show() {
        return this.params.stream()
                .anyMatch(p -> p.equals("show"));
    }

    boolean debt() {
        return this.params.stream()
                .anyMatch(p -> p.equals("debt"));
    }

    boolean pay() {
        return this.params.stream()
                .anyMatch(p -> p.equals("pay"));
    }

    /**
     * @return wallet ids
     */
    List<String> wallets() {
        return this.params.stream()
                .filter(p -> p.contains("wallet="))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .map(p -> Arrays.asList(p.split(",")))
                .orElse(Collections.emptyList());
    }

    boolean ignoreScoreWeakness() {
        return this.params.contains("ignore-score-weakness");
    }

    boolean ignoreNodesAbsence() {
        return this.params.contains("ignore-nodes-absence");
    }

    String privateKey() {
        return this.params.stream()
                .filter(p -> p.contains("private-key="))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Provide private key path "));
    }
}
