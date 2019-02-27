package io.triada.commands.clean;

import io.triada.commands.Command;
import io.triada.models.wallet.Copies;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

@AllArgsConstructor
public final class CleanCommand implements Command {

    private final Copies copies;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-clean")) {
            this.copies.clean();
        } else {
            throw new IllegalStateException("Command -r_elect was not provided");
        }
    }
}
