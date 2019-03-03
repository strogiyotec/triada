package io.triada.commands.propagate;

import com.google.common.base.Predicates;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
final class PropagateParams {
    private final List<String> params;

    public List<String> ids(final List<String> orElse) {
        return this.params.stream()
                .filter(p -> p.contains("ids="))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .filter(Predicates.not(String::isEmpty))
                .map(p -> Arrays.asList(p.split(",")))
                .orElse(orElse);
    }
}
