package io.triada.mocks;

import io.triada.Triada;
import io.triada.commands.ValuableCommand;
import io.triada.commands.node.NodeCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.models.wallet.EagerWallets;
import lombok.AllArgsConstructor;

import java.io.File;

@AllArgsConstructor
public final class FakeNode implements ValuableCommand<Integer> {

    private static final int PORT = 9000;

    private final File remotes;

    private final File wallets;

    private final File copies;

    private final String home;

    @Override
    public Integer run(final String[] argc) throws Exception {
        return new NodeCommand(
                new RemoteNodes(this.remotes),
                this.copies.toPath(),
                new EagerWallets(this.wallets)
        ).run(
                new String[]{
                        "-node",
                        "host=localhost",
                        "network=" + Triada.TEST_NETWORK,
                        "home=" + this.home,
                        "port=" + PORT,
                        "invoice=NOPREFIX@ffffffffffffffff"
                }
        );
    }
}
