package io.triada.commands.list;

import io.triada.commands.Command;
import io.triada.models.amount.TxnAmount;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Print list of wallets in given path
 */
@AllArgsConstructor
public final class ListCommand implements Command {

    /**
     * Wallets
     */
    private final Wallets wallets;

    /**
     * Root path to copies
     */
    private final Path copies;

    @Override
    public void run(final String[] argc) throws Exception {
        int total = 0;
        int txns = 0;
        int size = 0;
        TxnAmount balance = TxnAmount.ZERO;
        for (final String id : this.wallets.all()) {
            total++;
            final CopiesFromFile cps = new CopiesFromFile(this.cpsPath(id));
            final Wallet wallet = this.wallets.acq(id);
            final StringBuilder msg = new StringBuilder(
                    String.format(
                            "%s %dc",
                            wallet.mnemo(),
                            cps.all().size()
                    )
            );
            if (!wallet.head().network().equals(Wallet.MAINET)) {
                msg.append(
                        String.format(
                                " (net: %s)",
                                wallet.head().network()
                        )
                );
            }
            txns += wallet.transactions().size();
            balance = balance.add(wallet.balance().value());
            size += wallet.file().getTotalSpace();
            System.out.println(msg);
        }
        System.out.printf(
                "%d wallets,%d transactions, %d size, %s in total\n",
                total,
                txns,
                size,
                balance.asText(2)
        );
    }

    private Path cpsPath(final String id) throws IOException {
        final Path path = this.copies.resolve(id);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        return path;
    }
}
