package io.triada.commands.clean;

import io.triada.commands.Command;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.nio.file.Path;
import java.util.Optional;

@AllArgsConstructor
public final class CleanCommand implements Command {

    private final Path copies;

    private final Wallets wallets;

    public CleanCommand(final Path copies, final Path wallets) {
        this.copies = copies;
        this.wallets = new Wallets(wallets.toFile());
    }

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-clean")) {
            final CleanParams cleanParams = new CleanParams(cmd.getOptionValues("clean"));
            final Optional<String> walletId = cleanParams.walletId();
            if (walletId.isPresent()) {
                this.clean(
                        new CopiesFromFile(
                                this.copies.resolve(walletId.get())
                        ),
                        cleanParams)
                ;
            } else {
                for (final String id : this.wallets.all()) {
                    this.clean(
                            new CopiesFromFile(
                                    this.copies.resolve(id)
                            ),
                            cleanParams)
                    ;
                }
            }
        } else {
            throw new IllegalStateException("Command -clean was not provided");
        }
    }

    private void clean(final CopiesFromFile cps, final CleanParams params) throws Exception {
        final int deleted = cps.clean(params.maxAge());
        System.out.printf("%d copies were deleted\n", deleted);
    }
}
