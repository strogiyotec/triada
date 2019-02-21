package io.triada.models.score;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Load scores from file
 */
@UtilityClass
public final class ScoresFromFile {

    public List<Score> load(final File file) throws Exception {
        return lazyLoad(file)
                .collect(toList());
    }

    public Stream<Score> lazyLoad(final File file) throws Exception {
        return Files.lines(file.toPath()).map(TriadaScore::new);
    }
}
