package io.triada.commands.taxes;

import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.models.amount.TxnAmount;
import io.triada.models.tax.TxnTaxes;
import io.triada.models.wallet.Copies;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public final class TaxesCommand implements ValuableCommand<Optional<TxnAmount>> {

    private final Wallets wallets;

    private final Copies<File> copies;

    @Override
    public Optional<TxnAmount> run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-taxes")) {
            final TaxesParams taxesParams = new TaxesParams(Arrays.asList(cmd.getOptionValues("taxes")));
            if (taxesParams.show()) {
                this.show(taxesParams);
            } else if (taxesParams.debt()) {
                this.debt(taxesParams);
            } else if (taxesParams.pay()) {

            }
        } else {
            throw new IllegalArgumentException("Need to add taxes option");
        }
        return Optional.empty();
    }

    private void debt(final TaxesParams taxesParams) throws Exception {
        final List<String> wallets = taxesParams.wallets();
        if (wallets.isEmpty()) {
            System.out.println("At least one node should be provided");
        } else {
            for (final String wallet : wallets) {
                this.debt(wallet);
            }
        }
    }

    private void debt(final String wallet) throws Exception {
        final TxnTaxes txnTaxes = new TxnTaxes(this.wallets.acq(wallet));
        System.out.println(txnTaxes.debt());
        System.out.println(txnTaxes.asText());
    }

    public void show(final TaxesParams params) throws Exception {
        final List<String> wallets = params.wallets();
        if (wallets.isEmpty()) {
            System.out.println("At least one node should be provided");
        } else {
            for (final String wallet : wallets) {
                this.show(wallet);
            }
        }
    }

    private void show(final String wallet) throws Exception {
        System.out.println(new TxnTaxes(this.wallets.acq(wallet)).asText());
    }
}
