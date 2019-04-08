package io.triada.commands.push;

import com.google.common.base.Predicates;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
final class PushParams {

    private final List<String> params;

    boolean ignoreScoreWeekness() {
        return this.params.stream().anyMatch(param -> param.equals("ignore-score-weakness"));
    }

    boolean tolerateEdges() {
        return this.params.stream().anyMatch(param -> param.equals("tolerate-edges"));
    }

    boolean tolerateQuorum() {
        return this.params.stream().anyMatch(param -> param.equals("tolerate-quorum"));
    }

    boolean quietIfMissed() {
        return this.params.stream().anyMatch(param -> param.equals("quiet-if-missed"));
    }

    List<String> ids() {
        return this.params.stream()
                .filter(p -> p.contains("ids"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .filter(Predicates.not(String::isEmpty))
                .map(ids -> Arrays.asList(ids.split(",")))
                .orElse(Collections.emptyList());
    }

}
