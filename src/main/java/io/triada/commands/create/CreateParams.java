package io.triada.commands.create;

import io.triada.models.wallet.Wallet;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
final class CreateParams {

    private final List<String> params;


    String publicKey() {
        return this.params.stream()
                .filter(p -> p.contains("public-key"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Need to provide public key location "));
    }

    boolean skipTest() {
        return this.params.stream().anyMatch(p -> p.contains("skip-test"));
    }

    String network() {
        return this.params.stream()
                .filter(p -> p.contains("network"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst()
                .orElse(Wallet.MAINET);
    }

    Optional<String> id() {
        return this.params.stream()
                .filter(p -> p.contains("id"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .findFirst();
    }


}
