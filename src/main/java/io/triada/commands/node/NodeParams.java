package io.triada.commands.node;

import io.triada.commands.remote.RemoteNodes;
import io.triada.models.score.SuffixScore;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
final class NodeParams {

    private final List<String> params;

    String invoice() {
        return this.params.stream()
                .filter(p -> p.contains("invoice"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Need to provide invoice "));
    }

    String home() {
        return this.params.stream()
                .filter(p -> p.contains("home"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Need to provide home "));
    }

    int port() {
        return this.params.stream()
                .filter(p -> p.contains("port"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(RemoteNodes.PORT);
    }

    int bindPort() {
        return this.params.stream()
                .filter(p -> p.contains("bind-port"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(RemoteNodes.PORT);
    }

    boolean standalone() {
        return this.params.stream().anyMatch(p -> p.contains("standalone"));
    }

    boolean ignoreEmptyRemotes() {
        return this.params.stream().anyMatch(p -> p.contains("ignore-empty-remotes"));
    }

    String host() {
        return this.params.stream()
                .filter(p -> p.contains("host"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Need to provide host"));
    }

    String network() {
        return this.params.stream()
                .filter(p -> p.contains("network"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Need to provide host"));
    }

    public int strength() {
        return this.params.stream()
                .filter(p -> p.contains("strength"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(SuffixScore.STRENGTH);
    }
}
