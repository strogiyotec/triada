package io.triada.node.farm;

import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.functions.CheckedRunnable;
import io.triada.models.file.SyncFileWrite;
import io.triada.models.score.Score;
import io.triada.models.score.ScoresFromFile;
import io.triada.models.score.SuffixScore;
import io.triada.models.threads.Sleep;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

/**
 * Farm scores in single thread
 */
@AllArgsConstructor
public final class SingleThreadScoreFarm implements Farm {

    /**
     * File with farmed scores
     */
    private final File cache;

    /**
     * Lock for cache
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Strength of the score
     */
    private final int strength;

    /**
     * Score invoice
     */
    private final String invoice;


    /**
     * Run two threads , one of them find new suffixes , second one delete expired scores
     *
     * @param hostAndPort Host and port of node
     * @param runnable    Callback to call
     * @throws Exception if failed
     */
    @Override
    public void start(final HostAndPort hostAndPort, final CheckedRunnable runnable) throws Exception {
        PreloadFarmLog.log(this, this.cache);
        final ExecutorService service = Executors.newFixedThreadPool(2);
        this.clean(hostAndPort);
        service.submit(Unchecked.runnable(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                this.start();
            }
        }));
        service.submit(Unchecked.runnable(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                this.clean(hostAndPort);
            }
        }));
        try {
            runnable.run();
        } finally {
            service.shutdownNow();
            service.awaitTermination(1, TimeUnit.SECONDS);
            System.out.println("Farm was stopped after runnable execution");
        }

    }

    /**
     * Farm new scores
     *
     * @throws Exception if failed
     */
    private void start() throws Exception {
        lock.lock();
        try {
            final List<Score> load = this.best();
            assert !load.isEmpty();
            final Score next = load.get(0).next();
            SyncFileWrite.write(next.asText(), this.cache);
            System.out.printf(
                    "New score discovered %s\n",
                    next.asText()
            );
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clean expired scores
     *
     * @param hostAndPort Host and port of score
     * @throws Exception if failed
     */
    private void clean(final HostAndPort hostAndPort) throws Exception {
        final boolean lock = this.lock.tryLock();
        if (lock) {
            try {
                final List<Score> load =
                        ScoresFromFile.lazyLoad(this.cache)
                                .filter(p -> this.scoreValid(p, hostAndPort))
                                .collect(toList());
                if (load.isEmpty()) {
                    SyncFileWrite.write(
                            new SuffixScore(
                                    hostAndPort,
                                    this.invoice,
                                    this.strength
                            ).asText(),
                            this.cache
                    );
                }
                Sleep.withDuration(TimeUnit.SECONDS, 5);
            } finally {
                this.lock.unlock();
            }
        }
    }

    /**
     * @param score       Score to validate
     * @param hostAndPort Host and port
     * @return true if valid
     */
    private boolean scoreValid(final Score score, final HostAndPort hostAndPort) {
        return score.valid() &&
                score.address().equals(hostAndPort) &&
                !(score.strength() < this.strength);
    }

    /**
     * @return Sorted by value scores
     * @throws Exception if failed
     */
    @Override
    public List<Score> best() throws Exception {
        return ScoresFromFile
                .lazyLoad(this.cache)
                .sorted(comparingInt(Score::value).reversed())
                .collect(toList());
    }

    @Override
    public JsonObject asJson() {
        try {
            final JsonObject json = new JsonObject();
            json.addProperty("threads", 1);
            json.addProperty("best", new SuffixScore(FileUtils.readFileToString(this.cache, StandardCharsets.UTF_8)).mnemo());

            return json;
        } catch (final IOException exc) {
            throw new UncheckedIOException("Can't create farm json", exc);
        }
    }

    @Override
    public String asText() {
        return String.format(
                String.join(
                        "\n\n",
                        "Current time %s",
                        "Threads 1",
                        "Json: %s"
                ),
                new Date().toString(),
                this.asJson().toString()
        );
    }
}
