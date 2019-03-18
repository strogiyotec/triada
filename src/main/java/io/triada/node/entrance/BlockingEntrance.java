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
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;
import org.jooq.lambda.Seq;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

@AllArgsConstructor
public final class BlockingEntrance implements Entrance {

    private final Wallets wallets;

    private final Remotes remotes;

    private final Path copies;

    private final String address;

    private final String network;

    private final Path ledger;

    private final List<String> history = new ArrayList<>(16);

    private final List<Long> speed = new ArrayList<>(16);

    @Override
    public void start(final VoidYield runnable) throws Exception {
        runnable.run();
    }

    @Override
    public List<String> push(final String id, final String body) throws Exception {
        final CopiesFromFile copies = new CopiesFromFile(this.copies.resolve(id));
        final String host = "0.0.0.0";
        final long start = System.currentTimeMillis();
        copies.add(body, host, RemoteNodes.PORT, 0);
        if (!this.remotes.all().isEmpty()) {
            new FetchCommand(
                    this.wallets,
                    copies.root().toPath(),
                    this.remotes
            ).run(new String[]{
                    "-fetch",
                    "ignore-node=" + address,
                    "network=" + this.network,
                    "quiet-if-absent",
            });
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
        if (copies.all().size() == 1) {
            modified.add(id);
        }
        final long sec = System.currentTimeMillis() - start;
        if (this.history.size() >= 16) {
            this.history.remove(0);
        }
        if (this.speed.size() >= 64) {
            this.speed.remove(0);
        }
        final Wallet wallet = this.wallets.acq(id);
        this.history.add(
                String.format(
                        "%d/%d/%s",
                        sec,
                        modified.size(),
                        wallet.mnemo()
                )
        );
        this.speed.add(sec);
        return modified;
    }

    @Override
    public List<String> merge(final String id, final Copies<File> copies) throws Exception {
        final Path f = Files.createTempFile("ledger", "");
        final List<String> modified = new MergeCommand(
                this.wallets,
                this.remotes,
                copies.root().toPath()
        ).run(new String[]{
                "-merge",
                "ids=" + id,
                "ledger=" + f.toFile().getAbsolutePath(),
                "network=" + this.network
        });
        Files.write(
                this.ledger,
                this.content(f).getBytes(UTF_8)
        );


        return modified;
    }

    @Override
    public JsonObject asJson() {
        try {
            final JsonObject resJO = new JsonObject();
            resJO.addProperty("history", String.join(",", this.history));
            resJO.addProperty("historySize", this.history.size());
            resJO.addProperty("speed", this.speed.isEmpty() ? 0 : this.speed.stream().mapToLong(Long::longValue).sum() / this.speed.size());
            resJO.addProperty("ledger", Files.exists(this.ledger) ? Files.lines(this.ledger, StandardCharsets.UTF_8).count() : 0L);

            return resJO;
        } catch (final IOException exc) {
            throw new UncheckedIOException("Can't create json from entrance", exc);
        }
    }

    private String content(final Path f) throws IOException {
        final Seq<String> seq =
                Files.exists(this.ledger) ?
                        Seq.concat(
                                Files.lines(this.ledger, UTF_8),
                                Files.lines(f, UTF_8)) :
                        Seq.seq((Files.lines(f, UTF_8)));

        return seq.map(t -> t.split(";"))
                .distinct(t -> t[1] + "" + t[3])
                .filter(Predicates.not(t -> new Date(Long.parseLong(t[0])).compareTo(DateConverters.nowMinusHours(24)) >= 0))
                .map(t -> Stream.of(t).collect(Collectors.joining(";")))
                .toString("\n");
    }
}
