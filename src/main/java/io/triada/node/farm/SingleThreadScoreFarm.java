package io.triada.node.farm;

import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.models.file.SyncFileWrite;
import io.triada.models.score.Score;
import io.triada.models.score.ScoresFromFile;
import io.triada.models.score.TriadaScore;
import io.triada.models.threads.Sleep;
import lombok.AllArgsConstructor;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.lang.CheckedRunnable;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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


    @Override
    public void start(final HostAndPort hostAndPort, final CheckedRunnable runnable) throws Throwable {
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

    private void start() throws Exception {
        lock.lock();
        try {
            final List<Score> load = ScoresFromFile.load(this.cache);
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
                            new TriadaScore(
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

    @Override
    public List<Score> best() throws Exception {
        return ScoresFromFile.load(this.cache);
    }

    // TODO: 2/22/19 Implement
    @Override
    public JsonObject asJson() {
        return null;
    }

    // TODO: 2/22/19 Implement
    @Override
    public String asText() {
        return null;
    }
}
