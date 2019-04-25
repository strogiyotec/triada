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
                .addOption(remote())
                .addOption(
                        new Option("reset", false, "Restore it back to the default list of nodes")
                ).addOption(
                        new Option("masters", false, "Add all \"master\" nodes to the list")
                ).addOption(
                        Command.remoteAdd()
                )
                .addOption(
                        Command.node()
                )
                .addOption(
                        push()
                )
                .addOption(
                        elect()
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
                ).addOption(
                        new Option("ignore_score_weakness", false, "Skip score weakness")
                ).addOption(removeWallet())
                .addOption(clean())
                .addOption(fetch())
                .addOption(show())
                .addOption(calculate())
                .addOption(merge())
                .addOption(pay())
                .addOption(create())
                .addOption(invoice())
                .addOption(propagate())
                .addOption(taxes());
    }

    /**
     * @return Remote add option
     */
    static Option remoteAdd() {
        final Option option = new Option("radd", true, "Add new Remote");
        option.setArgs(2);
        option.setOptionalArg(true);
        option.setValueSeparator(' ');

        return option;
    }

    static Option elect() {
        final Option elect = new Option("r_elect", true, "Pick a random remote node as a target for a bonus awarding");
        elect.setArgs(1);
        elect.setOptionalArg(true);
        return elect;
    }

    static Option clean() {
        return getOption("clean", "Clean expired copies", 2);
    }

    static Option removeWallet() {
        final Option option = new Option("remove", true, "Remove wallet from dir");
        option.setArgs(1);
        option.setOptionalArg(true);
        option.setValueSeparator(' ');
        return option;
    }

    static Option show() {
        return getOption("show", "Sow balance of the wallet", 2);
    }

    static Option remote() {
        return getOption("remote", "Remote command", 10);
    }


    static Option fetch() {
        return getOption("fetch", "Fetch wallets from network", 10);
    }

    static Option push() {
        return getOption("push", "Push wallet to network", 10);
    }

    static Option create() {
        return getOption("create", "Create new wallet", 10);
    }


    static Option merge() {
        return getOption("merge", "Fetch wallets from network", 10);
    }

    static Option calculate() {
        return getOption("calculate", "Calculate score", 10);
    }


    static Option taxes() {
        return getOption("taxes", "Taxes Command", 10);
    }


    static Option invoice() {
        return getOption("invoice", "Generate invoice", 10);
    }

    static Option propagate() {
        return getOption("propagate", "Returns list of Wallet IDs which were affected", 1);
    }

    static Option node() {
        return getOption("node", "Run node ", 10);
    }


    static Option getOption(final String fetch, final String s, final int i) {
        final Option option = new Option(fetch, true, s);
        option.setArgs(i);
        option.setOptionalArg(true);
        option.setValueSeparator(' ');
        return option;
    }

    static Option pay() {
        return getOption("pay", "Money send command", 8);
    }

}
