package io.triada.models.wallet;

import io.triada.models.id.LongId;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public final class Wallets {

    private final File dir;

    /**
     * @return List of wallets IDs
     */
    public List<LongId> all() {
        return Stream.of(Objects.requireNonNull(this.dir.listFiles((dir, name) -> name.endsWith(TriadaWallet.EXT))))
                .map(file -> FilenameUtils.removeExtension(file.getName()))
                .map(LongId::new)
                .collect(Collectors.toList());
    }

    /**
     *
     * @return Amount of wallets
     */
    public int count() {
        return Objects.requireNonNull(this.dir.listFiles((dir, name) -> name.endsWith(TriadaWallet.EXT))).length;
    }
}
