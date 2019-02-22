package io.triada.node.farm;

import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.dates.DateConverters;
import io.triada.models.cli.CommandLineInterface;
import io.triada.models.cli.ShellScript;
import io.triada.models.file.SyncFileWrite;
import io.triada.models.score.ReducesScore;
import io.triada.models.score.Score;
import io.triada.models.score.ScoresFromFile;
import io.triada.models.score.TriadaScore;
import io.triada.threads.NamedThreadExecutor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.lang.CheckedRunnable;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

/**
 * Farm scores in background
 */
@Slf4j
public final class ScoreFarm implements Farm {

    private static final ThreadLocal<Date> start = new ThreadLocal<>();

    private static final ThreadLocal<Integer> threadIds = new ThreadLocal<>();

    private final File cache;

    private final String invoice;

    private final Deque<Score> pipeline;

    private final NamedThreadExecutor threads;

    private final int lifetime;

    private final int strength;

    private final Farms farms;

    private final CommandLineInterface<String> cli;

    public ScoreFarm(
            final File cache,
            final String invoice,
            final NamedThreadExecutor threads,
            final int lifetime,
            final int strength,
            final CommandLineInterface<String> cli,
            final Farms farms
    ) {
        this.cache = cache;
        this.invoice = invoice;
        this.pipeline = new ArrayDeque<>();
        this.threads = threads;
        this.lifetime = lifetime;
        this.strength = strength;
        this.cli = cli;
        this.farms = farms;
    }

    public ScoreFarm(
            final File cache,
            final String invoice,
            final NamedThreadExecutor threads,
            final int strength,
            final CommandLineInterface<String> cli,
            final Farms farms
    ) {
        this.cache = cache;
        this.invoice = invoice;
        this.pipeline = new ArrayDeque<>();
        this.threads = threads;
        this.lifetime = 24 * 60 * 60;
        this.strength = strength;
        this.cli = cli;
        this.farms = farms;
    }

    public ScoreFarm(
            final File cache,
            final String invoice,
            final NamedThreadExecutor threads,
            final CommandLineInterface<String> cli,
            final Farms farms
    ) {
        this.cache = cache;
        this.invoice = invoice;
        this.pipeline = new ArrayDeque<>();
        this.threads = threads;
        this.lifetime = 24 * 60 * 60;
        this.strength = TriadaScore.STRENGTH;
        this.cli = cli;
        this.farms = farms;
    }

    public ScoreFarm(
            final File cache,
            final String invoice,
            final NamedThreadExecutor threads,
            final Farms farms
    ) {
        this.cache = cache;
        this.invoice = invoice;
        this.pipeline = new ArrayDeque<>();
        this.threads = threads;
        this.lifetime = 24 * 60 * 60;
        this.strength = TriadaScore.STRENGTH;
        this.cli = new ShellScript();
        this.farms = farms;
    }

    public ScoreFarm(
            final File cache,
            final String invoice,
            final NamedThreadExecutor threads,
            final Farms farms,
            final int strength
    ) {
        this.cache = cache;
        this.invoice = invoice;
        this.pipeline = new ArrayDeque<>();
        this.threads = threads;
        this.lifetime = 24 * 60 * 60;
        this.strength = strength;
        this.cli = new ShellScript();
        this.farms = farms;
    }

    @Override
    public void start(
            final HostAndPort hostAndPort,
            final CheckedRunnable runnable
    ) throws Throwable {
        PreloadFarmLog.log(this, this.cache);

        final int threads = this.threads.getMaximumPoolSize();
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            this.threads.submit(
                    Unchecked.runnable(
                            () -> {
                                Thread.currentThread().setName(
                                        String.format(
                                                "f%d",
                                                threadId
                                        )
                                );
                                threadIds.set(threadId);
                                while (!Thread.currentThread().isInterrupted()) {
                                    this.cycle(hostAndPort, threads);
                                }
                            }
                    )
            );
        }
        if (threads != 0) {
            this.threads.submit(
                    Unchecked.runnable(
                            () -> {
                                while (!Thread.currentThread().isInterrupted()) {
                                    try {
                                        this.cleanUp(hostAndPort, threads);
                                        TimeUnit.SECONDS.sleep(1);
                                    } catch (final InterruptedException exc) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }
                    )
            );
        }
        this.cleanOnEmptyThreads(threads, hostAndPort);
        try {
            ScoreFarm.startAndShutdown(runnable, this.threads);
        } catch (final InterruptedException exc) {
            Thread.currentThread().interrupt();
        }

    }

    @Override
    public List<Score> best() throws Exception {
        return Files.lines(this.cache.toPath())
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

    private void cleanOnEmptyThreads(final int threads, final HostAndPort hostAndPort) throws Exception {
        if (threads <= 0) {
            this.cleanUp(hostAndPort, threads);
            System.out.printf(
                    "Farm started with no threads at %s:%d \n",
                    hostAndPort.getHost(),
                    hostAndPort.getPort()
            );
        } else {
            System.out.printf(
                    "Farm started with %d threads , one for cleanup at %s:%d strength is %d\n",
                    threads,
                    hostAndPort.getHost(),
                    hostAndPort.getPort(),
                    this.strength
            );
        }
    }

    private void cycle(final HostAndPort hostAndPort, final int threads) throws Exception {
        try {
            final Score score;
            while (true) {
                final Score temp = this.pipeline.pollLast();
                if (temp != null) {
                    score = temp;
                    break;
                } else {
                    TimeUnit.MILLISECONDS.sleep(250);
                }
            }
            System.out.println("Got value: " + score.asText() + " " + Thread.currentThread().getName());
            if (this.scoreValid(score, hostAndPort)) {
                System.out.println("It's valid");
                Thread.currentThread().setName(score.mnemo());
                ScoreFarm.start.set(new Date());
                final Score next = this.farms.up(score);
                System.out.printf(
                        "New score discovered %s %s\n",
                        next.asText(),
                        Thread.currentThread().getName()
                );
                this.save(threads, singletonList(next));
                this.cleanUp(hostAndPort, threads);
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean scoreValid(final Score score, final HostAndPort hostAndPort) {
        return score.valid() && score.address().equals(hostAndPort) && !(score.strength() < this.strength);
    }

    private void cleanUp(final HostAndPort hostAndPort, final int threads) throws Exception {
        final List<Score> scores = ScoresFromFile.load(this.cache);
        final int maxBefore =
                scores.stream()
                        .map(Unchecked.function(Score::value))
                        .max(Integer::compareTo)
                        .orElse(0);
        System.out.println("Max before " + maxBefore + " " + Thread.currentThread().getName());
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
        final List<Score> free =
                ScoresFromFile.load(this.cache)
                        .stream()
                        .filter(score -> {
                            final boolean b = !this.threads.exists(score.mnemo());
                            if (b) {
                                System.out.println("Score mnemo " + score.mnemo() + " " + Thread.currentThread().getName());
                            }
                            return b;
                        })
                        .collect(toList());

        if (this.pipeline.isEmpty() && !free.isEmpty()) {
            this.pipeline.add(free.get(0));
        }
        final int maxAfter =
                scores.stream()
                        .map(Unchecked.function(Score::value))
                        .max(Integer::compareTo)
                        .orElse(0);
        System.out.println("Max After " + maxAfter + " " + Thread.currentThread().getName());
        if (maxAfter != maxBefore && maxAfter != 0) {
            System.out.printf(
                    "%s : best score of %d is %s\n",
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
        final int period = this.lifetime / Math.max(threads, 1);
        final String body =
                seq(Stream.of(list, ScoresFromFile.load(this.cache))
                        .flatMap(List::stream))
                        .filter(Score::valid) //drop not valid
                        .filter(score -> !score.expired(TriadaScore.BEST_BEFORE))//drop expired
                        .filter(score -> score.strength() >= this.strength) // drop less strength
                        .sorted(Comparator.comparingInt(Score::value).reversed())
                        .distinct(Score::time)
                        .distinct(score -> score.age() / period)
                        .map(Score::asText)
                        .distinct()
                        .toString(System.lineSeparator());
        SyncFileWrite.write(body, this.cache);
    }

    private static JsonObject threadPoolJO(final ThreadPoolExecutor threads) {
        final JsonObject body = new JsonObject();
        body.addProperty("activeCount", threads.getActiveCount());
        body.addProperty("corePoolSize", threads.getCorePoolSize());
        body.addProperty("poolSize", threads.getPoolSize());

        return body;
    }

    /**
     * Start runnable and shutdown thread pool after runnable completion
     *
     * @param runnable Runnable to start
     * @param threads  Thread Pool
     * @throws InterruptedException if failed
     */
    private static void startAndShutdown(final Runnable runnable, final ThreadPoolExecutor threads) throws InterruptedException {
        try {
            runnable.run();
        } finally {
            threads.shutdownNow();
            threads.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
