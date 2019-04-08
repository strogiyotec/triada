package io.triada.commands.push;

import io.triada.commands.Command;
import io.triada.commands.remote.Remotes;
import io.triada.models.tax.TxnTaxes;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallet;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.Arrays;

@AllArgsConstructor
public final class PushCommand implements Command {

    private final EagerWallets wallets;

    private final Remotes remotes;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-push")) {
            final PushParams params = new PushParams(Arrays.asList(cmd.getOptionValues("push")));
            for (final String id : params.ids()) {
                this.push(id, params);
            }
        } else {
            throw new IllegalArgumentException("Need to add push option");
        }
    }

    private void push(final String id, final PushParams params) throws Exception {
        if (this.remotes.all().isEmpty()) {
            throw new IllegalStateException("No remotes");
        }
        final Wallet wallet = this.wallets.acq(id);
        if (!wallet.file().exists()) {
            throw new IllegalStateException("Wallet file doesn't exist");
        }
        if (new TxnTaxes(wallet).debt() > TxnTaxes.TRIAL.value()) {
            System.out.printf("Taxes %s are not paid , most likely the wallet won't be accepted", id);
        }

    }
}
