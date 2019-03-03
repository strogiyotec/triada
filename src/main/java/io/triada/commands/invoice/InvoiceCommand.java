package io.triada.commands.invoice;

import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.commands.remote.Remotes;
import io.triada.models.wallet.Copies;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.File;
import java.util.Arrays;

@AllArgsConstructor
public final class InvoiceCommand implements ValuableCommand<String> {

    private final Wallets wallets;

    private final Remotes remotes;

    private final Copies<File> copies;

    @Override
    public String run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-invoice")) {
            final InvoiceParams invoiceParams = new InvoiceParams(Arrays.asList(cmd.getOptionValues("invoice")));
            return this.invoice(invoiceParams);
        } else {
            throw new IllegalArgumentException("Need to add pay option");
        }
    }

    private String invoice(final InvoiceParams params) {
        final String receiver = params.receiverId();
        return null;
    }
}
