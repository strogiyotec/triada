package io.triada.commands.show;

import com.google.common.hash.Hashing;
import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.commands.list.ListCommand;
import io.triada.models.amount.Amount;
import io.triada.models.amount.TxnAmount;
import io.triada.models.tax.TxnTaxes;
import io.triada.models.wallet.*;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@AllArgsConstructor
public final class ShowCommand implements ValuableCommand<Amount<Long>> {

    private final Wallets wallets;

    private final Path copies;

    @Override
    public Amount<Long> run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-show")) {
            final ShowParams showParams = new ShowParams(Arrays.asList(cmd.getOptionValues("show")));
            final Optional<String> id = showParams.id();
            if (!id.isPresent()) {
                new ListCommand(this.wallets, this.copies).run(argc);
                return TxnAmount.ZERO;
            } else {
                final Wallet wallet = this.wallets.acq(id.get());
                return this.show(wallet);
            }
        } else {
            throw new IllegalArgumentException("Need to add show option");
        }
    }

    private Amount<Long> show(final Wallet wallet) throws Exception {
        final Amount<Long> balance = wallet.balance();
        final TxnTaxes taxes = new TxnTaxes(wallet);
        wallet.transactions().forEach(p -> System.out.println(p.asText()));
        System.out.println(
                String.join(
                        "\n",
                        String.format(
                                "The balance of %s is %s", wallet.head().id(), balance.asText(2)
                        ),
                        String.format(
                                "Network: %s",
                                wallet.head().network()
                        ),
                        String.format(
                                "Transactions: %d",
                                wallet.transactions().size()
                        ),
                        String.format(
                                "Taxes %d paid, the debt is %d",
                                taxes.paid(),
                                taxes.debt()
                        ),
                        String.format(
                                "File size : %d",
                                wallet.file().getTotalSpace()
                        ),
                        String.format(
                                "Modified at %s",
                                new Date(
                                        wallet.file().lastModified()
                                ).toString()
                        ),
                        String.format(
                                "Digest: %s",
                                Hashing.sha256()
                                        .hashBytes(Files.readAllBytes(wallet.file().toPath()))
                                        .toString()
                        )
                )
        );
        for (final WalletCopy copy : new CopiesFromFile(this.cpyPath(wallet)).all()) {
            System.out.printf(
                    "#%s : %d %s\n",
                    copy.name(),
                    copy.score(),
                    new EagerWallet(copy.path()).mnemo()
            );
        }
        return balance;
    }

    private Path cpyPath(final Wallet wallet) throws IOException {
        final Path path = this.copies.resolve(wallet.head().id());
        if(!Files.exists(path)){
            Files.createDirectory(path);
        }
        return path;
    }
}
