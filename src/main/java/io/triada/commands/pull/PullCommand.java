package io.triada.commands.pull;

import io.triada.commands.Command;
import io.triada.commands.clean.CleanCommand;
import io.triada.commands.fetch.FetchCommand;
import io.triada.commands.merge.MergeCommand;
import io.triada.commands.remote.Remotes;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

/**
 * Pull command
 */
@AllArgsConstructor
public final class PullCommand implements Command {

    private final Remotes remotes;

    private final Path wallets;

    private final Path copies;

    @Override
    public void run(final String[] argc) throws Exception {
        argc[0] = "-clean";
        new CleanCommand(this.copies, this.wallets).run(argc);
        argc[0] = "-fetch";
        new FetchCommand(this.wallets, this.copies, this.remotes).run(argc);
        argc[0] = "-merge";
        new MergeCommand(this.wallets, this.remotes, this.copies).run(argc);
    }
}
