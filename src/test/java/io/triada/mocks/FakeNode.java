package io.triada.mocks;

import io.triada.commands.Command;
import io.triada.commands.node.NodeCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.models.wallet.EagerWallets;
import lombok.AllArgsConstructor;

import java.io.File;

@AllArgsConstructor
public final class FakeNode implements Command {

    private static final int PORT = 9000;

    private final File remotes;

    private final File wallets;

    private final File copies;

    @Override
    public void run(final String[] argc) throws Exception {
        new NodeCommand(
                new RemoteNodes(this.remotes),
                this.copies.toPath(),
                new EagerWallets(this.wallets)
        ).run(
                new String[]{
                        "-node",
                        "host=localhost",
                        "port=" + PORT,
                        "invoice=NOPREFIX@ffffffffffffffff"
                }
        );
    }
}
