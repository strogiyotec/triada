package io.triada.node.farm;

import com.google.gson.JsonObject;
import io.triada.dates.DateConverters;
import io.triada.models.cli.CommandLineInterface;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
public final class ScoreFarm implements Farm {

    private final File cache;

    private final String invoice;

    private final Queue<String> pipeline;

    private final ThreadPoolExecutor threads;

    private final int lifetime;

    private final int strength;

    private final Farms farms;

    private final CommandLineInterface<String> cli;

    public ScoreFarm(
            final File cache,
            final String invoice,
            final Queue<String> pipeline,
            final ThreadPoolExecutor threads,
            final int lifetime,
            final int strength,
            final CommandLineInterface<String> cli,
            final Farms farms
    ) {
        this.cache = cache;
        this.invoice = invoice;
        this.pipeline = pipeline;
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
    public void save() {

    }

    @Override
    public List<Score> best() throws Exception {
        final String content = FileUtils.readFileToString(this.cache, StandardCharsets.UTF_8);

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
}
