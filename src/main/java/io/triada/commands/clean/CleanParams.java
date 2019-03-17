package io.triada.commands.clean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

final class CleanParams {

    private final List<String> params;

    public CleanParams(final String[] argc) {
        if (argc == null) {
            this.params = Collections.emptyList();
        } else {
            this.params = Arrays.asList(argc);
        }
    }

    public int maxAge() {
        return this.params.stream()
                .filter(p -> p.contains("max-age"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(1);
    }

    public Optional<String> walletId() {
        return this.params.stream()
                .filter(p -> p.contains("id"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst();
    }
}
