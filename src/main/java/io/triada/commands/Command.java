package io.triada.commands;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public interface Command {
    void run(String argc[]) throws Exception;

    /**
     * @return Predefined list of options
     */
    static Options options() {
        return new Options()
                .addOption(
                        new Option("rclean", false, "RemoveCommand all registered nodes")
                ).addOption(
                        new Option("show", false, "Show all registered remote nodes")
                ).addOption(
                        new Option("reset", false, "Restore it back to the default list of nodes")
                ).addOption(
                        new Option("masters", false, "Add all \"master\" nodes to the list")
                ).addOption(
                        Command.remoteAdd()
                ).addOption(
                        new Option("r_elect", false, "Pick a random remote node as a target for a bonus awarding")
                ).addOption(
                        new Option("trim", false, "RemoveCommand the least reliable nodes")
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
                ).addOption(
                        new Option("skip_ping", false, "Skip ping")
                ).addOption(removeWallet());
    }

    /**
     * @return Remote add option
     */
    static Option remoteAdd() {
        final Option option = new Option("radd", true, "Add new Remote");
        option.setArgs(2);
        option.setValueSeparator(' ');

        return option;
    }

    static Option removeWallet() {
        final Option option = new Option("remove", true, "Remove wallet from dir");
        option.setArgs(1);
        option.setOptionalArg(true);
        option.setValueSeparator(' ');
        return option;
    }

}
