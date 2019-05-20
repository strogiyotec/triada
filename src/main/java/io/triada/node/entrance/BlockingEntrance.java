package io.triada.node.entrance;

import com.google.common.base.Predicates;
import com.google.gson.JsonObject;
import io.triada.commands.clean.CleanCommand;
import io.triada.commands.fetch.FetchCommand;
import io.triada.commands.merge.MergeCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.commands.remote.Remotes;
import io.triada.dates.DateConverters;
import io.triada.functions.VoidYield;
import io.triada.models.wallet.Copies;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;
import org.jooq.lambda.Seq;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Long.parseLong;
import static java.nio.file.Files.*;
import static java.util.Arrays.asList;

@AllArgsConstructor
public final class BlockingEntrance implements Entrance {

    private final Wallets wallets;

    private final Remotes remotes;

    private final Path copies;

    private final String address;

    private final String network;

    private final Path ledger;

    public BlockingEntrance(
            final Wallets wallets,
            final Remotes remotes,
            final Path copies,
            final String address,
            final Path ledger
    ) {
        this.wallets = wallets;
        this.remotes = remotes;
        this.copies = copies;
        this.address = address;
        this.network = "test";
        this.ledger = ledger;
    }

    @Override
    public void start(final VoidYield runnable) throws Exception {
        runnable.run();
    }

    @Override
    public List<String> push(final String id, final String body, final List<String> params) throws Exception {
        final CopiesFromFile copies = new CopiesFromFile(this.walletCopiesDir(id));
        final String host = "0.0.0.0";
        copies.add(body, host, RemoteNodes.PORT, 0);
        if (!this.remotes.all().isEmpty()) {
            new FetchCommand(
                    this.wallets,
                    copies.root().toPath(),
                    this.remotes
            ).run(
                    newArrayList(
                            concat(
                                    asList(
                                            "-fetch",
                                            "ignore-node=" + address,
                                            "wallet=" + id,
                                            "network=" + this.network,
                                            "quiet-if-absent"
                                    ),
                                    params
                            )
                    ).toArray(new String[]{})
            );
        }
        final List<String> modified = this.merge(id, copies);
        new CleanCommand(copies.root().toPath(), this.wallets)
                .run(new String[]{
                        "-clean",
                        "id=" + id,
                        "max-age=" + 1
                });
        copies.remove(host, RemoteNodes.PORT);

        if (modified.isEmpty()) {
            System.out.printf("Accepted %s and not modified anything\n", id);
        } else {
            System.out.printf(
                    "Accepted %s and modified %s \n",
                    id,
                    String.join(",", modified)
            );
        }

        if (copies.all().size() > 1) {
            modified.add(id);
        }
        return modified;
    }

    @Override
    public List<String> merge(final String id, final Copies copies) throws Exception {
        final Path f = createTempFile("ledger", "");
        final Path t = createTempFile("trusted", "");
        try {
            final List<String> modified =
                    new MergeCommand(
                            this.wallets,
                            this.remotes,
                            copies.root().toPath()
                    ).run(new String[]{
                            "-merge",
                            "ids=" + id,
                            "trusted=" + t.toFile().getAbsolutePath(),
                            "ledger=" + f.toFile().getAbsolutePath(),
                            "network=" + this.network
                    });
            final List<String> txns = exists(this.ledger) ? readAllLines(this.ledger) : Collections.emptyList();
            txns.addAll(readAllLines(f));
            write(this.ledger, this.content(txns).getBytes(StandardCharsets.UTF_8));

            return modified;
        } finally {
            delete(f);
            delete(t);
        }
    }

    @Override
    public JsonObject asJson() {
        try {
            final JsonObject resJO = new JsonObject();
            resJO.addProperty("ledger", exists(this.ledger) ? lines(this.ledger, StandardCharsets.UTF_8).count() : 0L);

            return resJO;
        } catch (final IOException exc) {
            throw new UncheckedIOException("Can't create json from entrance", exc);
        }
    }

    private String content(final List<String> txns) {
        final Seq<String> seq = Seq.seq(txns);

        return seq.map(t -> t.split(";"))
                .distinct(t -> t[1] + "" + t[3])
                .filter(Predicates.not(t -> isDateAfter(t[0])))
                .map(t -> String.join(";", t))
                .toString("\n");
    }

    /**
     * @param id Wallet id
     * @return Copies dir for wallet with given id
     */
    private Path walletCopiesDir(final String id) throws IOException {
        final Path path = this.copies.resolve(id);
        if (!exists(path)) {
            return createDirectory(path);
        }
        return path;
    }

    private static boolean isDateAfter(final String unix) {
        return new Date(parseLong(unix)).compareTo(DateConverters.nowMinusHours(24)) <= 0;
    }
}
