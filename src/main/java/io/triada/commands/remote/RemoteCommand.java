package io.triada.commands.remote;

import io.triada.commands.Command;
import io.triada.node.farm.Farm;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Remote command
 */
@AllArgsConstructor
public final class RemoteCommand implements Command {

    private static final Options OPTIONS = RemoteCommand.remoteOptions();

    private final Remotes remotes;

    private final Farm farm;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(OPTIONS, argc);
        if (cmd.hasOption("remote clean")) {
            this.clean();
        }
        else if (cmd.hasOption("remote add")) {
            cmd.getOptionValue("");
        }
    }

    private void clean() throws Exception {
        int size = this.remotes.all().size();
        this.remotes.clean();
        System.out.printf(
                "All %d nodes deleted",
                size
        );
    }

    private static Options remoteOptions() {
        return new Options()
                .addOption(
                        Option.builder().valueSeparator(' ').argName("remote clean").desc("Remove all registered remote nodes").longOpt("remote clean").build()
                ).addOption(
                        new Option("show", false, "Show all registered remote nodes")
                ).addOption(
                        new Option("reset", false, "Restore it back to the default list of nodes")
                ).addOption(
                        new Option("masters", false, "Add all \"master\" nodes to the list")
                ).addOption(
                        Option.builder()
                                .argName("remote add")
                                .numberOfArgs(2)
                                .longOpt("remote add")
                                .desc("Add new node")
                                .build()
                ).addOption(
                        Option.builder()
                                .numberOfArgs(3)
                                .argName("remove")
                                .longOpt("remove")
                                .argName("host")
                                .longOpt("host")
                                .hasArgs()
                                .argName("port")
                                .longOpt("port")
                                .hasArgs()
                                .desc(" Remove the remote node")
                                .build()
                ).addOption(
                        new Option("elect", false, "Pick a random remote node as a target for a bonus awarding")
                ).addOption(
                        new Option("trim", false, "Remove the least reliable nodes")
                ).addOption(
                        Option.builder()
                                .numberOfArgs(2)
                                .argName("select")
                                .longOpt("select")
                                .argName("amount")
                                .longOpt("amount")
                                .hasArgs()
                                .desc("Select the strongest n nodes.")
                                .build()
                );
    }
}
