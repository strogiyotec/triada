package io.triada.commands.merge;

import com.google.common.base.Predicates;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
final class MergeParams {

    private final List<String> params;

    List<String> ids() {
        return this.params.stream()
                .filter(p -> p.contains("ids"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .filter(Predicates.not(String::isEmpty))
                .map(ids -> Arrays.asList(ids.split(",")))
                .orElse(Collections.emptyList());
    }

    boolean skipPropagate() {
        return this.params.stream().anyMatch(param -> param.equals("skip-propagate"));
    }

    boolean skipLegacy() {
        return this.params.stream().anyMatch(param -> param.equals("skip-legacy"));
    }

    boolean shallow() {
        return this.params.stream().anyMatch(param -> param.equals("shallow"));
    }

    String ledger() {
        return this.params.stream()
                .filter(p -> p.contains("ledger"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElse("/dev/null");
    }
}
