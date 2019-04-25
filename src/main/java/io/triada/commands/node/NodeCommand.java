package io.triada.commands.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import io.triada.Triada;
import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
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
import io.vertx.core.VertxOptions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Not thread safe
 */
@RequiredArgsConstructor
public final class NodeCommand implements ValuableCommand<Integer>, AutoCloseable {

    private final Remotes remotes;

    private final Path copies;

    private final EagerWallets wallets;

    private Vertx vertx;

    // TODO: 4/25/19 Need to add journal
    // TODO: 4/25/19 Clean copies
    @Override
    public Integer run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-node")) {
            final NodeParams nodeParams = new NodeParams(Arrays.asList(cmd.getOptionValues("node")));
            final HostAndPort hostAndPort = HostAndPort.fromParts(nodeParams.host(), nodeParams.port());
            final String address = String.format("%s:%d", hostAndPort.getHost(), hostAndPort.getPort());
            final String home = nodeParams.home();

            final Path ledger = Paths.get(home).resolve("ledger.csv");

            if (nodeParams.standalone()) {
                this.remotes.clean();
                System.out.println("Running in standalone mode will never talk to remotes");
            } else if (this.remotes.exists(hostAndPort.getHost(), hostAndPort.getPort())) {
                new RemoteCommand(this.remotes).run(new String[]{"-remote", "host=" + hostAndPort.getHost(), "port=" + hostAndPort.getPort()});
                System.out.printf("Removed current node %s \n", address);
            }
            /*if (Files.exists(this.copies)) {
                Files.delete(this.copies);
                System.out.printf("Directory %s deleted\n", this.copies.toString());
            }*/

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
                this.vertx = Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(1000 * 60 * 60));
                this.vertx.deployVerticle(new FrontPage(
                        ImmutableMap.of(
                                "protocol", Triada.PROTOCOL,
                                "version", Triada.VERSION
                        ),
                        farm,
                        ledger.toFile(),
                        new Wallets(this.wallets.dir()),
                        this.remotes,
                        hostAndPort.getPort(),
                        new BlockingEntrance(new Wallets(this.wallets.dir()), this.remotes, this.copies, address, ledger)
                ));
                System.out.println("Node was started");
            });
            return hostAndPort.getPort();
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

    @Override
    public void close() throws Exception {
        if (this.vertx != null) {
            this.vertx.close();
        }
    }
}