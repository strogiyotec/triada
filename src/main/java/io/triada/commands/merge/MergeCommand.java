package io.triada.commands.merge;

import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.commands.propagate.PropagateCommand;
import io.triada.commands.remote.Remotes;
import io.triada.models.patch.TxnsPatch;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.wallet.*;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
public final class MergeCommand implements ValuableCommand<List<String>> {

    private final Wallets wallets;

    private final Remotes remotes;

    private final Path copies;

    @Override
    public List<String> run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-merge")) {
            final MergeParams mergeParams = new MergeParams(Arrays.asList(cmd.getOptionValues("merge")));
            final List<String> ids = mergeParams.ids();
            final boolean skipPropagate = mergeParams.skipPropagate();
            final List<String> modified = new ArrayList<>(ids.size());
            final String[] propagate = propagate(argc);
            for (final String id : ids) {
                if (this.merge(id, new CopiesFromFile(this.copies.resolve(id)), mergeParams)) {
                    modified.add(id);
                    if (!skipPropagate) {
                        modified.addAll(
                                new PropagateCommand(
                                        new EagerWallets(
                                                this.wallets.dir()
                                        )
                                ).run(
                                        propagate
                                )
                        );
                    }
                }
            }
            return modified;
        } else {
            throw new IllegalArgumentException("Need to add merge option");
        }
    }

    private boolean merge(
            final String id,
            final CopiesFromFile copies,
            final MergeParams mergeParams
    ) throws Exception {
        final List<WalletCopy> cps = copies.all();
        cps.sort(Comparator.comparing(WalletCopy::score).reversed());
        final TxnsPatch patch = new TxnsPatch(this.wallets);
        int score = 0;
        if (!mergeParams.skipLegacy()) {
            mergeLegacy(id, patch, wallets);
        }
        int index = 0;
        for (final WalletCopy copy : copies.all()) {
            final TriadaWallet wallet = new TriadaWallet(copy.path());
            final String name =
                    String.format(
                            "%s/%d/%d",
                            copy.name(),
                            index,
                            copy.score()
                    );
            this.mergeOne(mergeParams, patch, wallet, name);
            score += copy.score();
            index++;
        }
        try {
            final Wallet wallet = this.wallets.acq(id);
            this.mergeOne(mergeParams, patch, wallet, "localhost");
            System.out.printf("Local copy of %s merged\n", id);
        } catch (final Exception exc) {
            System.out.printf("Local copy of %s is absent\n", id);
        }
        if (patch.empty()) {
            throw new IllegalStateException(
                    String.format(
                            "There are no copies of %s nothing to merge",
                            id
                    )
            );
        }
        final Wallet wallet = this.wallets.acq(id);
        final boolean modified = patch.save(wallet.file(), true);
        logModify(id, cps, score, wallet, modified);
        return modified;
    }

    private void mergeOne(
            final MergeParams mergeParams,
            final TxnsPatch patch,
            final Wallet wallet,
            final String name
    ) {
        try {
            System.out.printf(
                    "Building a patch for %s from remote copy %s with %s\n",
                    wallet.head().id(),
                    name,
                    wallet.mnemo()
            );
            if (mergeParams.shallow()) {
                patch.join(
                        wallet,
                        new File(mergeParams.ledger()),
                        signedTransaction -> {
                            final ParsedTxnData data = new ParsedTxnData(signedTransaction);
                            System.out.printf(
                                    "Paying wallet %s file is absent but it's a 'shallow' MERGE: %s\n",
                                    data.bnf().asText(),
                                    data.asText()
                            );
                            return false;
                        });
            } else {
                patch.join(
                        wallet,
                        new File(mergeParams.ledger()),
                        signedTransaction -> {
                            // TODO: 3/11/19 Need pull command
                            return true;
                        });
            }
            System.out.printf(
                    "Copy %s of %s merged\n",
                    name,
                    wallet.head().id()
            );
        } catch (final Exception exc) {
            System.out.printf(
                    "Can't merge copy %s : %s\n",
                    name, exc.getMessage()
            );
        }
    }

    private static void logModify(
            final String id,
            final List<WalletCopy> cps,
            final int score,
            final Wallet wallet,
            final boolean modified
    ) {
        if (modified) {
            System.out.printf(
                    "%d copies with total score of %d successfully merged into %s\n",
                    cps.size(),
                    score,
                    wallet.head().id()
            );
        } else {
            System.out.printf(
                    "Nothing changed in %s after merge of %d copies\n",
                    id,
                    cps.size()
            );
        }
    }

    private static void mergeLegacy(
            final String id,
            final TxnsPatch patch,
            final Wallets wallets
    ) throws Exception {
        final Wallet w = wallets.acq(id);
        patch.legacy(w);
        System.out.printf(
                "Local copy of %s merged legacy %s\n",
                id,
                patch.asText()
        );
    }

    private String[] propagate(final String[] argc) {
        final List<String> propagate = new ArrayList<>(Arrays.asList(argc));
        propagate.set(0, "-propagate");
        return propagate.toArray(new String[]{});
    }
}
