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
            if (data.amount().lessOrEq(0L)) {
                if (data.bnf().asText().equals(id)) {
                    System.out.printf("Pay itself in %s %s\n", id, data.asText());
                    continue;
                }
                final Wallet target = this.wallets.acq(data.bnf().asText());
                if (this.shouldSkip(id, network, data, target)) {
                    continue;
                }
                target.add(new SignedTriadaTxn(new InversedTxn(transaction, new LongId(id)), transaction.signature()));
                modified.add(data.bnf().asText());
            }
            total++;
        }
        System.out.printf("Wallet %s propagated successfully with %d txns\n", id, total);
        return modified;
    }

    private boolean shouldSkip(final String id, final String network, final ParsedTxnData data, final Wallet target) {
        if (!target.file().exists()) {
            System.out.printf("Wallet %s is absent\n", data.bnf().asText());
            return true;
        }
        if (!target.head().network().equals(network)) {
            System.out.println("Network mismatch");
            return true;
        }
        if (this.include(data.id(), id, target)) {
            System.out.printf("Wallet %s already has this transaction\n", target.head().id());
            return true;
        }
        if (!target.head().key().contains(data.prefix())) {
            System.out.println("Wrong prefix");
            return true;
        }
        return false;
    }

    private boolean include(final int id, final String bnf, final Wallet target) {
        return target.transactions().stream().anyMatch(txn -> {
            final ParsedTxnData txnData = new ParsedTxnData(txn);
            return id == txnData.id() && bnf.equals(txnData.bnf().asText()) && !txnData.amount().lessOrEq(0L);
        });
    }
}
