package io.triada.commands.show;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
final class ShowParams {

    final List<String> params;

    public Optional<String> id() {
        return this.params.stream()
                .filter(p -> p.contains("id"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst();
    }
}
