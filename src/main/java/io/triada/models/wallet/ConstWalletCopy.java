package io.triada.models.wallet;

import lombok.AllArgsConstructor;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;


@AllArgsConstructor
public final class ConstWalletCopy implements WalletCopy {

    private final String name;

    private final File path;

    private final int total;

    private final boolean master;

    private final int score;

    /**
     * Parse given entry and create instance of itself
     *
     * @param entry Entry line
     */
    public ConstWalletCopy(
            final Map.Entry<String, List<CsvCopy>> entry,
            final Path dir
    ) {
        this(
                entry.getKey(),
                dir.resolve(entry.getKey() + CopiesFromFile.EXT).toFile(),
                entry.getValue().size(),
                entry.getValue().stream().anyMatch(CsvCopy::master),
                entry.getValue().stream()
                        .filter(copy -> copy.time().compareTo(Date.from(Instant.now().minus(Duration.ofDays(1)))) >= 0)
                        .mapToInt(CsvCopy::score)
                        .sum()
        );
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public File path() {
        return this.path;
    }

    @Override
    public int total() {
        return this.total;
    }

    @Override
    public boolean master() {
        return this.master;
    }

    @Override
    public int score() {
        return this.score;
    }
}
