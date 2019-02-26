package io.triada.models.wallet;

import com.google.common.hash.Hashing;
import com.google.common.net.HostAndPort;
import io.triada.text.Text;
import lombok.AllArgsConstructor;
import org.jooq.lambda.Seq;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FilenameUtils.removeExtension;

@AllArgsConstructor
public final class CopiesFromFile implements Copies {

    private static final String EXT = ".zc";

    private final Path dir;

    @Override
    public String add(
            final String content,
            final HostAndPort hostAndPort,
            final int score,
            final Date time,
            final boolean master
    ) throws IOException {
        final String name;
        final Optional<CsvCopy> target =
                this.load()
                        .stream()
                        .filter(Unchecked.predicate(
                                csv -> {
                                    final Path file = this.dir.resolve(csv.name() + EXT);
                                    final String digest = Hashing.sha256().hashString(content, StandardCharsets.UTF_8).toString();
                                    return Files.exists(file) && Hashing.sha256().hashBytes(Files.readAllBytes(file)).toString().equals(digest);
                                })
                        ).findFirst();
        if (target.isPresent()) {
            name = target.get().name();
        } else {
            final int max = Stream.of(requireNonNull(this.dir.toFile().listFiles((dir, fileName) -> removeExtension(fileName).matches("^[0-9]+$"))))
                    .mapToInt(file -> Integer.parseInt(removeExtension(file.getName())))
                    .max()
                    .orElse(0);
            name = String.valueOf(max + 1);
            Files.write(this.dir.resolve(name + EXT), content.getBytes(StandardCharsets.UTF_8));
        }
        this.save(
                Seq.seq(this.load().stream())
                        .filter(csv -> !csv.host().equals(hostAndPort.getHost()) && csv.port() != hostAndPort.getPort())//remove element with same host and port
                        .append(new ConstCsvCopy(
                                name,
                                hostAndPort.getHost(),
                                hostAndPort.getPort(),
                                score,
                                time,
                                master
                        ))
                        .map(Text::asText)
                        .toList()
        );
        return name;

    }

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

    @Override
    public int clean() throws IOException {
        final List<CsvCopy> list =
                this.load()
                        .stream()
                        .filter(copy -> copy.time().compareTo(new Date(System.currentTimeMillis() - 24 * 60 * 60)) >= 0)
                        .collect(Collectors.toList());
        this.rewrite(list.stream().map(Text::asText).collect(Collectors.toList()));
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

    @Override
    public List<AllCopy> all() throws IOException {
        final Map<String, List<CsvCopy>> groupBy =
                this.load()
                        .stream()
                        .collect(Collectors.groupingBy(CsvCopy::name));
        return groupBy.entrySet().stream().map(line ->
                new ConstAllCopy(
                        line.getKey(),
                        this.dir.resolve(line.getKey() + EXT).toFile(),
                        line.getValue().size(),
                        line.getValue().stream().anyMatch(CsvCopy::master),
                        line.getValue().stream()
                                .filter(copy -> copy.time().compareTo(new Date(System.currentTimeMillis() - 24 * 60 * 60)) >= 0)
                                .mapToInt(CsvCopy::score)
                                .sum()
                )).collect(Collectors.toList());
    }

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
                )).collect(Collectors.toList());
    }

    private Path file() {
        return this.dir.resolve("scores" + EXT);
    }

    private File[] files() {
        return this.dir.toFile().listFiles((dir, name) -> removeExtension(name).matches("^[0-9]+$"));
    }

    private void save(final List<String> copies) throws IOException {
        Files.write(this.file(), copies, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    private void rewrite(final List<String> copies) throws IOException {
        Files.write(this.file(), copies, StandardCharsets.UTF_8);
    }

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
