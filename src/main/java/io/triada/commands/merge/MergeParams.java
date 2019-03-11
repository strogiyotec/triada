package io.triada.commands.merge;

import com.google.common.base.Predicates;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
final class MergeParams {

    private final List<String> params;

    public List<String> ids() {
        return this.params.stream()
                .filter(p -> p.contains("ids"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .filter(Predicates.not(String::isEmpty))
                .map(ids -> Arrays.asList(ids.split(",")))
                .orElse(Collections.emptyList());
    }

    public boolean skipPropagate() {
        return this.params.stream().anyMatch(param -> param.equals("skip-propagate"));
    }

    public boolean skipLegacy() {
        return this.params.stream().anyMatch(param -> param.equals("skip-legacy"));
    }

    public boolean shallow() {
        return this.params.stream().anyMatch(param -> param.equals("shallow"));
    }

    public String ledger() {
        return this.params.stream()
                .filter(p -> p.contains("ledger"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Need to provide ledger"));
    }
}
