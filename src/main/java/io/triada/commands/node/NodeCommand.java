package io.triada.commands.node;

import com.google.common.collect.ImmutableMap;
import io.triada.Triada;
import io.triada.commands.Command;
import io.triada.commands.invoice.InvoiceCommand;
import io.triada.commands.remote.RemoteCommand;
import io.triada.commands.remote.Remotes;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallets;
import io.triada.node.entrance.BlockingEntrance;
import io.triada.node.farm.SingleThreadScoreFarm;
import io.triada.node.front.FrontPage;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@AllArgsConstructor
public final class NodeCommand implements Command {

    private final Remotes remotes;

    private final Path copies;

    private final EagerWallets wallets;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-node")) {
            final NodeParams nodeParams = new NodeParams(Arrays.asList(cmd.getOptionValues("node")));
            final String host = nodeParams.host();
            final int port = nodeParams.port();
            final String address = String.format("%s:%d", host, port);
            final String home = nodeParams.home();
            final Path journal = Paths.get(home).resolve(".triadadata/journal");
            Files.createDirectory(journal);
            final Path ledger = Paths.get(home).resolve("ledger.csv");
            if (nodeParams.standalone()) {
                this.remotes.clean();
                System.out.println("Running in standalone mode will never talk to remotes");
            } else if (this.remotes.exists(host, port)) {
                new RemoteCommand(this.remotes).run(new String[]{"-remote", "host=" + host, "port=" + port});
                System.out.printf("Removed current node %s \n", address);
            }
            if (Files.exists(this.copies)) {
                Files.delete(this.copies);
                System.out.printf("Directory %s deleted\n", this.copies.toString());
            }
            final BlockingEntrance blockingEntrance =
                    new BlockingEntrance(
                            new Wallets(this.wallets),
                            this.remotes,
                            this.copies,
                            address,
                            nodeParams.network(),
                            ledger
                    );
            blockingEntrance.start(() -> {
                final SingleThreadScoreFarm farm =
                        new SingleThreadScoreFarm(
                                Paths.get(home).resolve("farm").toFile(),
                                nodeParams.strength(),
                                invoice(nodeParams)
                        );
                Vertx.vertx()
                        .deployVerticle(new FrontPage(
                                ImmutableMap.of(
                                        "protocol", Triada.PROTOCOL,
                                        "version", Triada.VERSION
                                ),
                                farm,
                                ledger.toFile(),
                                new Wallets(this.wallets.dir()),
                                this.remotes,
                                8080,
                                new BlockingEntrance(new Wallets(this.wallets.dir()), this.remotes, this.copies, address, ledger)
                        ));
                System.out.println("Node was started");
            });

        } else {
            throw new IllegalArgumentException("Add node param");
        }
    }

    private String invoice(final NodeParams params) throws Exception {
        final String invoice = params.invoice();
        if (!invoice.contains("@")) {
            return new InvoiceCommand(
                    new Wallets(this.wallets.dir()),
                    this.remotes,
                    new CopiesFromFile(this.copies)
            ).run(new String[]{
                    "-invoice",
                    "network=" + params.network(),
                    "tolerate-quorum"
            });
        } else {
            return invoice;
        }
    }
}