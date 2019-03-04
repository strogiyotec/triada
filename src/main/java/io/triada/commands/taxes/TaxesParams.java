package io.triada.commands.taxes;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
final class TaxesParams {

    private final List<String> params;

    public boolean show() {
        return this.params.stream()
                .anyMatch(p -> p.equals("show"));
    }

    public boolean debt() {
        return this.params.stream()
                .anyMatch(p -> p.equals("debt"));
    }

    public boolean pay() {
        return this.params.stream()
                .anyMatch(p -> p.equals("pay"));
    }

    /**
     * @return wallet ids
     */
    public List<String> wallets() {
        return this.params.stream()
                .filter(p -> p.contains("wallet="))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .map(p -> Arrays.asList(p.split(",")))
                .orElse(Collections.emptyList());
    }

    public boolean ignoreScoreWeakness() {
        return this.params.contains("ignore-score-weakness");
    }
}
