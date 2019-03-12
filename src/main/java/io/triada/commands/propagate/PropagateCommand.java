package io.triada.commands.propagate;

import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.models.id.LongId;
import io.triada.models.transaction.InversedTxn;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallet;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// TODO: 3/3/19 Not ready , need Pay command

/**
 * Add transactions from payer to receiver
 * Return list of Wallet ID's which were affected
 */
@AllArgsConstructor
public final class PropagateCommand implements ValuableCommand<List<String>> {

    private final EagerWallets wallets;

    @Override
    public List<String> run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-propagate")) {
            final PropagateParams propagateParams = new PropagateParams(Arrays.asList(cmd.getOptionValues("propagate")));
            final List<String> ids = propagateParams.ids(this.wallets.all());
            final List<String> modified = new ArrayList<>(ids.size());
            for (final String id : propagateParams.ids(this.wallets.all())) {
                modified.addAll(this.propagate(id));
            }
            return modified;
        } else {
            throw new IllegalArgumentException("Need to add propagate option");
        }

    }

    private List<String> propagate(final String id) throws Exception {
        final List<String> modified = new ArrayList<>(16);
        final String network = this.wallets.acq(id).head().network();
        int total = 0;
        for (final SignedTransaction transaction : this.wallets.acq(id).transactions()) {
            final ParsedTxnData data = new ParsedTxnData(transaction);
            total++;
            if (data.bnf().asText().equals(id)) {
                System.out.printf("Pay itself in %s %s\n", id, data.asText());
                continue;
            }
            final Wallet target = this.wallets.acq(data.bnf().asText());
            if (!target.file().exists()) {
                System.out.printf("Wallet %s is absent\n", data.bnf().asText());
                continue;
            }
            if (!target.head().network().equals(network)) {
                System.out.println("Network mismatch");
                continue;
            }
            // !txns.find { |t| t.id == id && t.bnf == bnf && !t.amount.negative? }.nil?
            if (target.transactions().stream().anyMatch(txn -> {
                final ParsedTxnData txnData = new ParsedTxnData(txn);
                return data.id() == txnData.id() && data.bnf().asText().equals(txnData.bnf().asText());
            })) {
                continue;
            }
            if (!target.head().key().contains(data.prefix())) {
                System.out.println("Wrong prefix");
            }
            target.add(new SignedTriadaTxn(new InversedTxn(transaction, new LongId(id)), transaction.signature()));
            modified.add(data.bnf().asText());
        }
        System.out.printf("Wallet %s propagated successfully with %d txns\n", id, total);
        return modified;
    }
}
