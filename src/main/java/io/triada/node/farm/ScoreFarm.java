package io.triada.node.farm;

import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.dates.DateConverters;
import io.triada.models.cli.CommandLineInterface;
import io.triada.models.score.ReducesScore;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Seq;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Farm scores in background
 */
@Slf4j
public final class ScoreFarm implements Farm {

    private final File cache;

    private final String invoice;

    private final Queue<Score> pipeline;

    private final ThreadPoolExecutor threads;

    private final int lifetime;

    private final int strength;

    private final Farms farms;

    private final CommandLineInterface<String> cli;

    public ScoreFarm(
            final File cache,
            final String invoice,
            final ThreadPoolExecutor threads,
            final int lifetime,
            final int strength,
            final CommandLineInterface<String> cli,
            final Farms farms
    ) {
        this.cache = cache;
        this.invoice = invoice;
        this.pipeline = new ArrayBlockingQueue<>(1);
        this.threads = threads;
        this.lifetime = lifetime;
        this.strength = strength;
        this.cli = cli;
        this.farms = farms;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void cleanUp() throws Exception {

    }

    @Override
    public List<Score> best() throws Exception {
        final String content = FileUtils.readFileToString(this.cache, StandardCharsets.UTF_8);

        if (content.isEmpty()) {
            return Collections.emptyList();
        }

        return Stream.of(content.split(System.lineSeparator()))
                .map(TriadaScore::new)
                .collect(toList());
    }


    @Override
    public JsonObject asJson() {
        final JsonObject body = new JsonObject();
        body.add("threads", threadPoolJO(this.threads));
        body.addProperty(
                "best",
                Unchecked.supplier(
                        () ->
                                best().stream()
                                        .map(Score::mnemo)
                                        .collect(Collectors.joining(", "))
                ).get());
        body.addProperty("pipeline", this.pipeline.size());
        body.addProperty("farmer", this.farms.getClass().getSimpleName());

        return body;
    }

    @Override
    public String asText() {
        return String.format(
                "Current time: %s \nTriada processes : %s",
                DateConverters.asIso(new Date()),
                Unchecked.supplier(() -> cli.executeCommand("ps ax | grep triada | wc -l")).get()
        );
    }

    private static JsonObject threadPoolJO(final ThreadPoolExecutor threads) {
        final JsonObject body = new JsonObject();
        body.addProperty("activeCount", threads.getActiveCount());
        body.addProperty("corePoolSize", threads.getCorePoolSize());
        body.addProperty("poolSize", threads.getPoolSize());

        return body;
    }

    private void cycle(final HostAndPort hostAndPort, final ExecutorService threads) {

    }


    private void cleanUp(final HostAndPort hostAndPort, final int threads) throws Exception {
        List<Score> scores = this.load();
        final int maxBefore =
                scores.stream()
                        .map(Unchecked.function(Score::value))
                        .max(Integer::compareTo)
                        .orElse(0);
        this.save(
                threads,
                singletonList(
                        new TriadaScore(
                                hostAndPort,
                                this.invoice,
                                this.strength
                        )
                )
        );
        // TODO: 2/16/19 add mnimo check
        scores = this.load();
        if (this.pipeline.isEmpty() && !scores.isEmpty()) {
            this.pipeline.add(scores.get(0));
        }
        final int maxAfter =
                scores.stream()
                        .map(Unchecked.function(Score::value))
                        .max(Integer::compareTo)
                        .orElse(0);
        if (maxAfter != maxBefore && maxAfter != 0) {
            System.out.printf(
                    "%s : best score of %d is %s",
                    Thread.currentThread().getName(),
                    scores.size(),
                    new ReducesScore(4, scores.get(0)).asText()
            );
        }

    }

    /**
     * Merge given list with list of scores from cache ot single list
     * Filter this list and merge all unique scores to single String representation
     * Finally append created String to file
     *
     * @param threads amount of threads
     * @param list    List of new scores
     * @throws Exception if failed
     */
    private void save(final int threads, final List<Score> list) throws Exception {
        final List<Score> scores = Stream.of(list, this.load()).flatMap(List::stream).collect(toList());
        final int period = this.lifetime / Math.max(threads, 1);
        final String body = Seq.seq(scores.stream())
                .filter(Score::valid) //drop not valid
                .filter(score -> !score.expired(TriadaScore.BEST_BEFORE))//drop expired
                .filter(score -> score.strength() >= this.strength) // drop less strength
                .sorted(Comparator.comparingInt(Score::value).reversed())
                .distinct(Score::time)
                .distinct(score -> score.age() / period)
                .map(Score::asText)
                .distinct()
                .toString(System.lineSeparator());
        ScoreFarm.syncSaveScores(body, this.cache);
    }

    /**
     * Requires exclusive lock to file and append body
     *
     * @param body Text to append
     * @param file File to append
     * @throws Exception if failed
     */
    private static void syncSaveScores(final String body, final File file) throws Exception {
        try (final RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            try (final FileChannel channel = accessFile.getChannel()) {
                try (final FileLock lock = channel.lock()) {
                    try (final FileWriter writer = new FileWriter(file, true)) {
                        writer.append(body);
                    }
                }
            }
        }
    }

    private List<Score> load() throws Exception {
        return Files.lines(this.cache.toPath())
                .map(TriadaScore::new)
                .collect(toList());
    }
}
