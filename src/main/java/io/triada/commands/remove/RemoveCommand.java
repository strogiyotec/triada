package io.triada.commands.remove;

import io.triada.commands.Command;
import io.triada.models.wallet.TriadaWallet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Delete wallet file
 */
public final class RemoveCommand implements Command {

    /**
     * Options
     */
    private static final Options OPTIONS = Command.options();

    /**
     * List of wallet files
     */
    private final List<File> wallets;

    public RemoveCommand(final File path) {
        this.wallets =
                Arrays.asList(
                        Objects.requireNonNull(
                                path.listFiles(file -> file.getName().endsWith(TriadaWallet.EXT)
                                )
                        )
                );
    }

    /**
     * Get id of wallet and try to delete associated file
     * if id is not present delete all wallet files
     *
     * @param argc Command line argc
     * @throws Exception if failed
     */
    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(OPTIONS, argc);
        if (cmd.hasOption("-remove")) {
            final String id = cmd.getOptionValue("remove");
            if (id != null) {
                this.deleteSingleFile(id);
            } else {
                final int before = this.wallets.size();
                this.wallets.forEach(File::delete);
                System.out.printf(
                        "%d files was deleted \n",
                        before
                );
            }
        }
    }

    /**
     * Delete file with given name
     *
     * @param fileName FileName to delete
     */
    private void deleteSingleFile(final String fileName) {
        final Optional<File> file = this.wallets.stream().filter(p -> FilenameUtils.removeExtension(p.getName()).equals(fileName)).findFirst();
        if (file.isPresent() && file.get().delete()) {
            System.out.printf("File %s was deleted\n", fileName);
        } else {
            System.out.printf("No file with fileName %s\n", fileName);
        }
    }
}
