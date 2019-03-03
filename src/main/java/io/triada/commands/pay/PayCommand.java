package io.triada.commands.pay;

import io.triada.commands.Command;
import io.triada.models.amount.TxnAmount;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.Arrays;

/**
 * Money sending command
 * Usage: triada pay wallet target amount [details] [options]
 * Where:
 * 'wallet' is the sender's wallet ID
 * 'target' is the beneficiary (either wallet ID or invoice number)'
 * 'amount' is the amount to pay, for example: '14.95TRD' (in TRD) or '12345trdz' (in tridz)
 * 'details' is the optional text to attach to the payment
 */
final class PayCommand implements Command {
    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-pay")) {
            final PayParams params = new PayParams(Arrays.asList(cmd.getOptionValues("pay")));
            final String id = params.payerWalletId();
            // TODO: 3/3/19 check on @
            final String invoice = params.invoice();
            final TxnAmount amount = params.amount();
            final String details = params.details();

        } else {
            throw new IllegalArgumentException("Need to add pay option");
        }
    }
}
