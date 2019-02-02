package io.triada.node.farm;

import com.google.gson.JsonObject;
import io.triada.dates.DateConverters;
import io.triada.models.cli.CommandLineInterface;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public final class ScoreFarm implements Farm {

    private final File cache;

    private final String invoice;

    private final Queue<String> pipeline;

    private final ThreadPoolExecutor threads;

    private final int lifetime;

    private final int strength;

    private final CommandLineInterface<String> cli;

    public ScoreFarm(
            final File cache,
            final String invoice,
            final Queue<String> pipeline,
            final ThreadPoolExecutor threads,
            final int lifetime,
            final int strength,
            final CommandLineInterface<String> cli
    ) {
        this.cache = cache;
        this.invoice = invoice;
        this.pipeline = pipeline;
        this.threads = threads;
        this.lifetime = lifetime;
        this.strength = strength;
        this.cli = cli;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void cleanUp() throws Exception {

    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }

    @Override
    public JsonObject asJson() {
        return null;
    }

    @Override
    public String asText() {
        return String.format(
                "Current time: %s \nTriada processes : %s",
                DateConverters.asIso(new Date()),
                Unchecked.supplier(() -> cli.executeCommand("ps ax | grep triada | wc -l")).get()
        );
    }
}
