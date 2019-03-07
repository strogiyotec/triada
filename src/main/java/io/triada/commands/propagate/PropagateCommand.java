package io.triada.commands.propagate;

import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.transaction.InversedTxn;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.File;
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
            return this.propagate(propagateParams);
        } else {
            throw new IllegalArgumentException("Need to add propagate option");
        }

    }

    private List<String> propagate(final PropagateParams params) throws Exception {
        final List<String> ids = params.ids(this.wallets.all());
        final List<String> modified = new ArrayList<>(16);
        for (final String id : ids) {
            final Wallet wallet = this.wallets.acq(id);
            int total = 0;
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
                if (!network.equals(wallet.head().network())) {
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
                final LongId walletId = new LongId(id);
                target.add(
                        new SignedTriadaTxn(
                                new InversedTxn(
                                        data,
                                        walletId
                                ),
                                new RsaKey(
                                        new File(params.privateKey())
                                ),
                                walletId
                        )
                );
                System.out.printf(
                        "%d arrived to %s\n",
                        data.amount().value() * -1,
                        data.bnf().asText()
                );
                modified.add(data.bnf().asText());
            }
            System.out.printf("Wallet %s propagated, %d txns", wallet.head().id(), total);
        }
        return modified;
    }
}
