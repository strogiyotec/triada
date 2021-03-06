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

    boolean edgeBaseline() {
        return this.params.stream().anyMatch(param -> param.equals("edge-baseline"));
    }

    boolean quietIfAbsent() {
        return this.params.stream().anyMatch(param -> param.equals("quiet-if-absent"));
    }

    boolean noBaseline() {
        return this.params.stream().anyMatch(param -> param.equals("no-baseline"));
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

    String trusted() {
        return this.params.stream()
                .filter(p -> p.contains("trusted"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElse("/dev/null");
    }

    String network() {
        return this.params.stream()
                .filter(p -> p.contains("network"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElse("test");
    }

    int depth() {
        return this.params.stream()
                .filter(p -> p.contains("depth"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .map(Integer::parseInt)
                .orElse(0);
    }

    int maxTrusted() {
        return this.params.stream()
                .filter(p -> p.contains("trusted-max"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .map(Integer::parseInt)
                .orElse(128);
    }
}
