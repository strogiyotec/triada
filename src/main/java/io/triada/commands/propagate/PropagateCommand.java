package io.triada.commands.propagate;

import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.models.id.LongId;
import io.triada.models.transaction.InversedTxn;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// TODO: 3/3/19 Not ready , need Pay command

/**
 * Return list of Wallet ID's which were affected
 */
@AllArgsConstructor
public final class PropagateCommand implements ValuableCommand<List<String>> {

    private final Wallets wallets;

    @Override
    public List<String> run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-propagate")) {
            final PropagateParams propagateParams = new PropagateParams(Arrays.asList(cmd.getOptionValues("propagate")));
            return this.propagate(propagateParams.ids(this.wallets.all()));
        } else {
            throw new IllegalArgumentException("Need to add propagate option");
        }

    }

    private List<String> propagate(final List<String> ids) throws Exception {
        final List<String> modified = new ArrayList<>(16);
        int total = 0;
        for (final String id : ids) {
            final Wallet wallet = this.wallets.acq(id);
            for (final SignedTransaction txn : wallet.transactions()) {
                total++;
                final ParsedTxnData data = new ParsedTxnData(txn);
                if (data.bnf().equals(new LongId(id))) {
                    System.out.printf("Paint itself in %s %s\n", id, data.asText());
                    continue;
                }
                final Wallet target = this.wallets.acq(data.bnf().asText());
                final String network = target.head().network();
                final String propagateNetwork = wallet.head().network();
                if (!target.head().network().equals(wallet.head().network())) {
                    System.out.printf(
                            "Network mismatch %s!=%s",
                            network,
                            propagateNetwork
                    );
                    continue;
                }
                if (!target.exists(data.id(), data.bnf())) {
                    continue;
                }
                if (!target.prefix(data.prefix())) {
                    System.out.printf(
                            "Wrong prefix %s in %s\n",
                            data.prefix(),
                            data.asText()
                    );
                    continue;
                }
                target.add(
                        new SignedTriadaTxn(
                                new InversedTxn(data, new LongId(id)),
                                null,
                                null
                        ));
            }
        }
        return null;
    }
}
