package io.triada.commands.create;

import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.commands.remote.Remotes;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.wallet.EmptyWallet;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Create new wallet
 */
@AllArgsConstructor
public final class CreateCommand implements ValuableCommand<String> {

    /**
     * Wallets
     */
    private final Wallets wallets;

    /**
     * Remotes
     */
    private final Remotes remotes;

    @Override
    public String run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-create")) {
            final CreateParams params = new CreateParams(Arrays.asList(cmd.getOptionValues("create")));
            final Optional<String> id = params.id();
            return this.create(
                    params,
                    id.isPresent() ? id.get() : this.createId(params)
            );
        } else {
            throw new IllegalArgumentException("Need to add create option");
        }
    }

    private String create(final CreateParams params, final String id) throws Exception {
        final RsaKey key = new RsaKey(new File(params.publicKey()));
        final Path file = this.wallets.dir().toPath().resolve(id + TriadaWallet.EXT);
        final EmptyWallet wallet = new EmptyWallet(
                id,
                key.asPublic(),
                params.network(),
                file.toFile()
        );
        System.out.printf(
                "Wallet %s was created at %s\n",
                wallet.asText(),
                file.toAbsolutePath().toString()
        );
        return id;
    }

    /**
     * Create new wallet id and check if remotes already have this id,
     * If id is not unique recalculate it again
     *
     * @param params cli params
     * @return New Id
     * @throws Exception if failed
     */
    private String createId(final CreateParams params) throws Exception {
        while (true) {
            final String id = new LongId().asText();
            if (params.skipTest()) {
                return id;
            }
            final AtomicBoolean find = new AtomicBoolean(false);
            this.remotes.modify(
                    remoteNode -> {
                        try {
                            remoteNode.http(String.format("/wallet/%s/digest", id)).get();
                            find.set(true);
                        } catch (final Exception exc) {
                            System.out.printf(
                                    "Remote node %s doesn't contain wallet with id %s\n",
                                    remoteNode.asText(),
                                    id
                            );
                        }
                    }
            );
            if (!find.get()) {
                return id;
            }
            System.out.printf("Wallet id %s is already occupied , witll try another one..\n", id);
        }
    }
}
