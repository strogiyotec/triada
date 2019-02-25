package io.triada.models.wallet;

import com.google.common.net.HostAndPort;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class CopiesFromFile implements Copies {

    private final File file;

    @Override
    public void add(final String content, final HostAndPort hostAndPort, final int score, final Date time, final boolean master) {

    }

    @Override
    public void remove(final HostAndPort hostAndPort) throws IOException {
        final List<CsvCopy> load = this.load();
        try (final FileWriter writer = new FileWriter(this.file, false)) {
            for (final CsvCopy csvCopy : load) {
                if (csvCopy.port() != hostAndPort.getPort() && !csvCopy.host().equals(hostAndPort.getHost())) {
                    writer.append(csvCopy.asText());
                }
            }
        }
    }

    @Override
    public void clean() {

    }

    @Override
    public List<AllCopy> all() {
        return null;
    }

    @Override
    public List<CsvCopy> load() throws IOException {
        return Files.lines(this.file.toPath())
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
}
