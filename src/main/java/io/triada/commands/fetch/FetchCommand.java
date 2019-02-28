package io.triada.commands.fetch;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import io.triada.commands.Command;
import io.triada.commands.remote.RemoteNode;
import io.triada.commands.remote.Remotes;
import io.triada.models.score.AssertScore;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import io.triada.models.wallet.AllCopy;
import io.triada.models.wallet.Copies;
import io.triada.models.wallet.Wallets;
import io.triada.node.farm.Farm;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.fi.util.function.CheckedBiFunction;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.triada.http.HttpTriadaClient.READ_TIMEOUT;

@AllArgsConstructor
public final class FetchCommand implements Command {

    private final Wallets wallets;

    private final Path copies;

    private final Remotes remotes;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-fetch")) {
            final List<String> params = Arrays.asList(cmd.getOptionValues("fetch"));

            final AtomicInteger nodes = new AtomicInteger(0);
            final AtomicInteger total = new AtomicInteger(0);
            final AtomicInteger done = new AtomicInteger(0);
            final AtomicInteger masters = new AtomicInteger(0);
            this.remotes.modify(remoteNode -> {
                nodes.incrementAndGet();
                total.incrementAndGet();
                if (remoteNode.isMaster()) {
                    masters.incrementAndGet();
                }
                done.incrementAndGet();
            }, Farm.EMPTY);
            if (nodes.get() == 0) {
                throw new IllegalStateException("No nodes");
            }
            if (masters.get() == 0 && Stream.of(cmd.getOptionValues("-fetch")).noneMatch(p -> p.equals("tolerate_edges"))) {
                throw new IllegalStateException("There are no masternodes");
            }
            final int quorum = tolerateQuorum(params);
            if (nodes.get() < quorum) {
                throw new IllegalStateException(
                        String.format(
                                "There are not enough nodes, the required quorum is %d",
                                quorum)
                );
            }

        } else {
            throw new IllegalArgumentException("Need to add fetch option");
        }
    }

    private int fetchOne(final String id, final RemoteNode remoteNode, final Copies copies, final List<String> params) throws Exception {
        final String remoteStr = remoteNode.asText();
        if (ignoreNodes(params).contains(remoteStr)) {
            System.out.printf("Node %s was ignored because of ignore-nod option", remoteStr);
            return 0;
        }
        this.readOne(id, remoteNode, 0, new CheckedBiFunction<JsonObject, Score, Integer>() {
            @Override
            public Integer apply(final JsonObject jsonObject, final Score score) throws Exception {
                AssertScore.assertValidScore(score);
                AssertScore.assertScoreOwnership(score, remoteNode.address());
                if (!params.contains("ignore-score-weakness")) {
                    AssertScore.assertScoreStrength(score);
                }
                final List<AllCopy> all = copies.all();
                String copy = null;
                for (final AllCopy allCopy : all) {
                    if (jsonObject.get("digest")
                            .getAsString()
                            .equals(
                                    Hashing.sha256()
                                            .hashBytes(
                                                    Files.readAllBytes(
                                                            allCopy.path().toPath()
                                                    )
                                            ).toString()
                            )
                            && jsonObject.get("size").getAsInt() == allCopy.path().getTotalSpace()) {
                        continue;
                    }
                    copy = copies.add(
                            FileUtils.readFileToString(allCopy.path(), StandardCharsets.UTF_8),
                            score.address(),
                            score.value(),
                            remoteNode.isMaster()
                    );
                    System.out.printf(
                            "No need to fetch %s from %s it's the same content as copy %s",
                            id,
                            remoteNode.asText(),
                            copy
                    );
                    break;
                }
                if(copy == null){

                }
                return null;
            }
        })

    }

    // TODO: 2/28/19 Add retry logic
    public int readOne(
            final String id,
            final RemoteNode remoteNode,
            final int retries,
            final CheckedBiFunction<JsonObject, Score, Integer> yield
    ) throws Exception {
        try {
            final String url = "/wallet/" + id;
            final JsonObject body = remoteNode.http(url).get(READ_TIMEOUT);
            final TriadaScore score = new TriadaScore(body.get("score").getAsJsonObject());
            return yield.apply(body, score);
        } catch (final Throwable exc) {
            // TODO: 2/28/19 Retry
            throw new Exception(exc);
        }
    }

    /**
     * @param params Cli params
     * @return tolerateQuorum number from params
     */
    private static int tolerateQuorum(final List<String> params) {
        return params.stream()
                .filter(param -> param.contains("tolerate-quorum"))
                .findFirst()
                .map(quorum -> quorum.substring(quorum.indexOf("=") + 1))
                .map(Integer::parseInt)
                .orElse(0);
    }

    /**
     * @param params CLi params
     * @return List of ignore nodes which are separated by ','
     */
    private static List<String> ignoreNodes(final List<String> params) {
        return params.stream().filter(p -> p.contains("ignore-node"))
                .map(p -> p.substring(p.indexOf("=") + 1))
                .map(p -> Arrays.asList(p.split(",")))
                .findFirst()
                .orElse(Collections.emptyList());
    }
}
