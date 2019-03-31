package io.triada.commands.fetch;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import io.triada.Triada;
import io.triada.commands.Command;
import io.triada.commands.remote.RemoteNode;
import io.triada.commands.remote.Remotes;
import io.triada.models.score.AssertScore;
import io.triada.models.score.Score;
import io.triada.models.score.SuffixScore;
import io.triada.models.wallet.*;
import io.triada.node.farm.Farm;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.fi.util.function.CheckedFunction;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static io.triada.http.HttpTriadaClient.READ_TIMEOUT;
import static io.triada.models.id.LongId.ROOT;

/**
 * Fetch wallets from remotes
 */
@AllArgsConstructor
public final class FetchCommand implements Command {

    /**
     * Wallets
     */
    private final Wallets wallets;

    /**
     * Copies path
     */
    private final Path copies;

    /**
     * Remote nodes
     */
    private final Remotes remotes;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-fetch")) {
            final FetchParams params = new FetchParams(Arrays.asList(cmd.getOptionValues("fetch")));
            for (final String id : params.wallets(this.wallets.all())) {
                this.fetch(params, id, new CopiesFromFile(copies.resolve(id)));
            }
        } else {
            throw new IllegalArgumentException("Need to add fetch option");
        }
    }

    private void fetch(final FetchParams params, final String id, final Copies cps) throws Exception {
        if (this.remotes.all().isEmpty()) {
            if (params.quietIfAbsent()) {
                return;
            } else {
                throw new IllegalStateException("There are no remote nodes");
            }
        }
        // TODO: 3/2/19 Why nodes and done ??
        final AtomicInteger nodes = new AtomicInteger(0);
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger done = new AtomicInteger(0);
        final AtomicInteger masters = new AtomicInteger(0);
        this.remotes.modify(remoteNode -> {
            nodes.incrementAndGet();
            total.addAndGet(this.fetchOne(id, remoteNode, cps, params));
            done.incrementAndGet();
            if (remoteNode.isMaster()) {
                masters.incrementAndGet();
            }
        }, Farm.EMPTY);
        if (nodes.get() == 0) {
            throw new IllegalStateException("No nodes");
        }
        if (masters.get() == 0 && !params.tolerateEdges()) {
            throw new IllegalStateException("There are no masternodes");
        }
        final int quorum = params.tolerateQuorum();
        if (nodes.get() < quorum) {
            throw new IllegalStateException(
                    String.format(
                            "There are not enough nodes, the required quorum is %d",
                            quorum)
            );
        }
        System.out.printf(
                "%d copies of %s fetched with total score %d from %d nodes and %d master nodes\n",
                done.get(),
                id,
                total.get(),
                nodes.get(),
                masters.get()
        );

    }

    /**
     * @param id         Wallet id
     * @param remoteNode Remote node
     * @param copies     Copies
     * @param argc       cli argc
     * @return Score value of given remote node
     */
    private int fetchOne(
            final String id,
            final RemoteNode remoteNode,
            final Copies copies,
            final FetchParams argc
    ) {
        final String remoteStr = remoteNode.asText();
        if (argc.ignoreNodes().contains(remoteStr)) {
            System.out.printf("Node %s was ignored because of ignore-node option\n", remoteStr);
            return 0;
        }
        return readOne(
                id,
                remoteNode,
                0,
                this.fda(id, remoteNode, copies, argc));
    }

    /**
     * @param id         Wallet id
     * @param remoteNode Remote node
     * @param copies     Copies
     * @param argc       cli argc
     * @return Score value of remote node
     */
    private CheckedFunction<JsonObject, Integer> fda(
            final String id,
            final RemoteNode remoteNode,
            final Copies copies,
            final FetchParams argc
    ) {
        return jsonObject -> {
            final Score score = new SuffixScore(jsonObject.get("score").getAsJsonObject());
            assertScore(remoteNode, argc, score);
            String copy = null;
            for (final WalletCopy walletCopy : copies.all()) {
                final String content = FileUtils.readFileToString(walletCopy.path(), StandardCharsets.UTF_8);
                if (walletAlreadyExists(jsonObject, content)) {
                    continue;
                }
                copy = copies.add(
                        FileUtils.readFileToString(walletCopy.path(), StandardCharsets.UTF_8),
                        score.address(),
                        score.value(),
                        remoteNode.isMaster()
                );
                System.out.printf(
                        "No need to fetch %s from %s it's the same content as copy %s \n",
                        id,
                        remoteNode.asText(),
                        copy
                );
                break;
            }
            if (copy == null) {
                final File file = remoteNode.http(String.format("/wallet/%s.bin", id)).getFile(File.createTempFile("wallet", TriadaWallet.EXT));
                final Wallet wallet = new TriadaWallet(file);
                assertWallet(id, argc, wallet);
                final String name = copies.add(
                        FileUtils.readFileToString(file, StandardCharsets.UTF_8),
                        score.address(),
                        score.value(),
                        remoteNode.isMaster()
                );
                System.out.printf("Copy of wallet %s was saved in file %s", id, name);
            }
            return score.value();
        };
    }

    /**
     * @param id     Walled id
     * @param argc   cli argc
     * @param wallet Wallet
     */
    private static void assertWallet(
            final String id,
            final FetchParams argc,
            final Wallet wallet
    ) {
        if (!wallet.head().protocol().equals(Triada.PROTOCOL)) {
            throw new IllegalStateException(
                    String.format(
                            "Protocol %s doesn't match %s in %s",
                            wallet.head().protocol(),
                            Triada.PROTOCOL,
                            id
                    )
            );
        }
        final String network = argc.network();
        if (!wallet.head().network().equals(network)) {
            throw new IllegalStateException(
                    String.format(
                            "Protocol is %s , but we need %s ",
                            wallet.head().protocol(),
                            network
                    )
            );
        }
        if (wallet.balance().less(0L) && !wallet.head().id().equals(String.valueOf(ROOT.id()))) {
            throw new IllegalStateException(
                    String.format(
                            "The balance of %s is negative and it's not root",
                            id)
            );
        }
    }

    /**
     * @param jsonObject JsonObject
     * @param content    Wallet Copy content
     * @return True if value 'digest' in jsonObject is the same as SHA-256 hashed content
     */
    private static boolean walletAlreadyExists(
            final JsonObject jsonObject,
            final String content
    ) {
        return jsonObject.get("digest")
                .getAsString()
                .equals(Hashing.sha256().hashString(content, StandardCharsets.UTF_8).toString());
    }

    /**
     * Assert given score
     *
     * @param remoteNode Node
     * @param argc       cli argc
     * @param score      Score
     */
    private static void assertScore(
            final RemoteNode remoteNode,
            final FetchParams argc,
            final Score score
    ) {
        AssertScore.assertValidScore(score);
        AssertScore.assertScoreOwnership(score, remoteNode.address());
        if (!argc.ignoreScoreWeakness()) {
            AssertScore.assertScoreStrength(score);
        }
    }

    // TODO: 2/28/19 Add retry logic

    /**
     * Get Json response from given remote node
     * Call provided yield
     *
     * @param id         Wallet id
     * @param remoteNode REmote node
     * @param retries    amount of retries
     * @param yield      Callback
     * @return value returned from callback
     */
    private static int readOne(
            final String id,
            final RemoteNode remoteNode,
            final int retries,
            final CheckedFunction<JsonObject, Integer> yield
    ) {
        try {
            final String url = "wallet/" + id;
            final JsonObject body = remoteNode.http(url).get(READ_TIMEOUT);
            return yield.apply(body);
        } catch (final Throwable exc) {
            // TODO: 2/28/19 Retry
            return 0;
        }
    }


}
