package io.triada.commands.calculate;

import io.triada.commands.remote.RemoteNodes;
import io.triada.models.score.TriadaScore;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
final class CalculateParams {

    private final List<String> params;

    public int strength() {
        return this.params.stream()
                .filter(p -> p.contains("strength"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(TriadaScore.STRENGTH);
    }

    public Date time() {
        return this.params.stream()
                .filter(p -> p.contains("time"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .map(Long::parseLong)
                .map(Date::new)
                .findFirst()
                .orElse(new Date());
    }

    public String invoice() {
        return this.params.stream()
                .filter(p -> p.contains("invoice"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Need to provide invoice "));
    }

    public String host() {
        return this.params.stream()
                .filter(p -> p.contains("host"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElse("127.0.01");
    }

    public int port() {
        return this.params.stream()
                .filter(p -> p.contains("port"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(RemoteNodes.PORT);
    }

    public int max() {
        return this.params.stream()
                .filter(p -> p.contains("max"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(8);
    }

    public boolean hideHash() {
        return this.params.stream().anyMatch(p -> p.contains("hide-hash"));
    }

    public boolean hideTime() {
        return this.params.stream().anyMatch(p -> p.contains("hide-time"));
    }
}
