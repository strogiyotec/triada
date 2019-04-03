package io.triada.node.farm;

import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.functions.CheckedRunnable;
import io.triada.models.score.Score;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Run given origin in background using {@link java.util.concurrent.ExecutorService}
 */
@AllArgsConstructor
public final class AsyncFarm implements Farm {

    /**
     * Origin
     */
    private final Farm origin;

    /**
     * Executor service
     */
    private final ExecutorService executorService;

    @Override
    public void start(final HostAndPort hostAndPort, final CheckedRunnable runnable) throws Exception {
        this.executorService.submit(() -> {
            this.origin.start(hostAndPort, runnable);
            return null;//because we need callable to don't catch exception
        });
    }

    @Override
    public List<Score> best() throws Exception {
        return this.origin.best();
    }

    @Override
    public JsonObject asJson() {
        return this.origin.asJson();
    }

    @Override
    public String asText() {
        return this.origin.asText();
    }
}
