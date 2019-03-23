package io.triada.routine;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
final class GcParams {
    private final List<String> params;

    public boolean routineImmediately() {
        return this.params.contains("routine-immediately");
    }

    public long gcAge() {
        return this.params
                .stream()
                .filter(p -> p.contains("gc-age"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .map(Long::parseLong)
                .orElseThrow(() -> new IllegalArgumentException("Need to provide gc-age"));
    }
}
