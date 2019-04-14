package io.triada.models.wallet;

import com.google.common.hash.Hashing;
import com.google.common.net.HostAndPort;
import io.triada.text.Text;
import lombok.AllArgsConstructor;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * This class contains copies of wallets
 * One file with name 'scores' is a csv file with names of each wallet fail .For example name = 1
 * that means , we have file with name '1' in the dir , this file represent wallet with wallet information like head and transactions
 */
@AllArgsConstructor
public final class CopiesFromFile implements Copies {

    public static final String EXT = ".zc";

    private final Path dir;

    @Override
    public File root() {
        return dir.toFile().getParentFile();
    }

    /**
     * Add new Copy to given dir
     * If file with given content already exists return name of this file
     * Otherwise create new file with given content
     * Finally Save new wallet copy's name to scores.zc file
     *
     * @param content     Content
     * @param hostAndPort HostAndPort
     * @param score       Score
     * @param time        Time
     * @param master      Master
     * @return Name
     * @throws IOException if failed
     */
    @Override
    public String add(
            final String content,
            final HostAndPort hostAndPort,
            final int score,
            final Date time,
            final boolean master
    ) throws IOException {
        final String name;
        final Optional<CsvCopy> target = this.contentExists(content);
        if (target.isPresent()) {
            name = target.get().name();
        } else {
            final int max =
                    Stream.of(this.files())
                            .mapToInt(file -> Integer.parseInt(removeExtension(file.getName())))
                            .max()
                            .orElse(0);
            name = String.valueOf(max + 1);
            Files.write(this.dir.resolve(name + EXT), content.getBytes(StandardCharsets.UTF_8));
        }
        final List<CsvCopy> load = this.load();
        logDeleted(
                load.removeIf(
                        csv -> csv.port() == hostAndPort.getPort() && csv.host().equals(hostAndPort.getHost())
                ),
                hostAndPort
        );
        load.add(new ConstCsvCopy(
                name,
                hostAndPort.getHost(),
                hostAndPort.getPort(),
                score,
                time,
                master
        ));
        this.rewrite(load.stream().map(CsvCopy::asText).collect(toList()));
        return name;

    }

    /**
     * @param content Content
     * @return Optional of csv file if csv file content os equals to fiven content
     * @throws IOException if failed
     */
    private Optional<CsvCopy> contentExists(final String content) throws IOException {
        return this.load()
                .stream()
                .filter(Unchecked.predicate(
                        csv -> {
                            final Path file = this.dir.resolve(csv.name() + EXT);
                            final String digest =
                                    Hashing.sha256()
                                            .hashString(content, StandardCharsets.UTF_8)
                                            .toString();
                            return Files.exists(file) &&
                                    Hashing.sha256()
                                            .hashBytes(Files.readAllBytes(file))
                                            .toString()
                                            .equals(digest);
                        })
                )
                .findFirst();
    }

    /**
     * Remote copy with given host and port
     *
     * @param hostAndPort To remove
     * @throws IOException if failed
     */
    @Override
    public void remove(final HostAndPort hostAndPort) throws IOException {
        final List<CsvCopy> load = this.load();
        boolean deleted = false;
        try (final FileWriter writer = new FileWriter(this.file().toFile(), false)) {
            for (final CsvCopy csvCopy : load) {
                if (csvCopy.port() != hostAndPort.getPort() || !csvCopy.host().equals(hostAndPort.getHost())) {
                    writer.append(csvCopy.asText()).append("\n");
                } else {
                    deleted = true;
                }
            }
        }
        logDeleted(deleted, hostAndPort);
    }

    /**
     * Delete files with content which were added using add method, main file (scores.zc) still exists
     *
     * @return number of deleted files
     * @throws IOException if failed
     */
    @Override
    public int clean(final int days) throws IOException {
        final List<CsvCopy> list =
                this.load()
                        .stream()
                        .filter(copy -> copy.time().compareTo(Date.from(Instant.now().minus(Duration.ofDays(days)))) >= 0)
                        .collect(toList());
        this.rewrite(
                list.stream()
                        .map(Text::asText)
                        .collect(toList())
        );
        int deleted = 0;
        for (final File file : this.files()) {
            if (list.stream().anyMatch(csv -> csv.name().equals(removeExtension(file.getName())))) {
                if (file.delete()) {
                    System.out.printf("File %s was deleted\n", file);
                }
                deleted++;
            }
        }
        return deleted;
    }

    /**
     * Group copies by name and collect them to list of {@link WalletCopy}
     *
     * @return List of allCopies
     * @throws IOException if failed
     */
    @Override
    public List<WalletCopy> all() throws IOException {
        final Map<String, List<CsvCopy>> groupBy =
                this.load()
                        .stream()
                        .collect(Collectors.groupingBy(CsvCopy::name));
        return groupBy.entrySet()
                .stream()
                .map(line -> new ConstWalletCopy(line, this.dir))
                .sorted(
                        Comparator.comparing(WalletCopy::master)
                                .thenComparing(WalletCopy::score)
                                .reversed()
                ).collect(toList());
    }

    /**
     * Load {@link CsvCopy} from scores file
     *
     * @return List of {@link CsvCopy}
     * @throws IOException if failed
     */
    @Override
    public List<CsvCopy> load() throws IOException {
        final Path file = this.file();
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        return Files.lines(file)
                .map(line -> line.split(","))
                .map(split -> new ConstCsvCopy(
                        split[0],
                        split[1],
                        Integer.parseInt(split[2]),
                        Integer.parseInt(split[3]),
                        new Date(Long.parseLong(split[4])),
                        split[5].equals("M")
                )).collect(toList());
    }

    /**
     * @return Scores file
     */
    private Path file() {
        return this.dir.resolve("scores" + EXT);
    }

    /**
     * @return Array of files with saved content @see add
     */
    private File[] files() {
        return this.dir.toFile().listFiles((dir, name) -> removeExtension(name).matches("^[0-9]+$"));
    }

    /**
     * Rewrite scores with given Content
     *
     * @param copies Content
     * @throws IOException if failed
     */
    private void rewrite(final List<String> copies) throws IOException {
        Files.write(this.file(), copies, StandardCharsets.UTF_8);
    }

    /**
     * Log if file was deleted
     *
     * @param deleted     Deleted
     * @param hostAndPort HostAndPort
     */
    private static void logDeleted(final boolean deleted, final HostAndPort hostAndPort) {
        if (deleted) {
            System.out.printf(
                    "Copy with host %s and port %d was deleted\n",
                    hostAndPort.getHost(),
                    hostAndPort.getPort()
            );
        } else {
            System.out.printf(
                    "Copy with host %s and port %d doesn't exist\n",
                    hostAndPort.getHost(),
                    hostAndPort.getPort()
            );
        }
    }
}
