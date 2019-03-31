package io.triada.commands.pay;

import io.triada.commands.Command;
import io.triada.commands.invoice.InvoiceCommand;
import io.triada.commands.propagate.PropagateCommand;
import io.triada.commands.remote.Remotes;
import io.triada.commands.taxes.TaxesCommand;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.tax.TxnTaxes;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.wallet.Copies;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.File;
import java.util.Arrays;

import static com.google.common.collect.Iterables.getLast;

/**
 * Money sending command
 * Usage: triada pay wallet target amount [details] [options]
 * Where:
 * 'wallet' is the sender's wallet ID
 * 'target' is the beneficiary (either wallet ID or invoice number)'
 * 'amount' is the amount to pay, for example: '14.95TRD' (in TRD) or '12345trdz' (in tridz)
 * 'details' is the optional text to attach to the payment
 */
@AllArgsConstructor
public final class PayCommand implements Command {

    private final Wallets wallets;

    private final Remotes remotes;

    private final Copies copies;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-pay")) {
            final PayParams params = new PayParams(Arrays.asList(cmd.getOptionValues("pay")));
            final String id = params.payerWalletId();
            this.taxes(id, params);
            this.pay(this.wallets.acq(id), params);
            new PropagateCommand(new EagerWallets(this.wallets.dir()))
                    .run(
                            new String[]{
                                    "-propagate",
                                    "ids=" + id
                            }
                    );
        } else {
            throw new IllegalArgumentException("Need to add pay option");
        }
    }

    private void taxes(
            final String walletId,
            final PayParams payParams
    ) throws Exception {
        final Wallet wallet = this.wallets.acq(walletId);
        final boolean debt = new TxnTaxes(wallet).debt() > TxnTaxes.TRIAL.value() && !payParams.dontPayTaxes();
        if (debt) {
            new TaxesCommand(
                    this.wallets,
                    this.remotes
            ).run(
                    new String[]{
                            "taxes",
                            "pay",
                            "private-key=" + payParams.privateKey(),
                            "wallet=" + walletId
                    }
            );
        }
    }

    private void pay(final Wallet from, final PayParams params) throws Exception {
        // TODO: 3/8/19 Add invoice command
        final String invoice = this.normalizedInvoice(params.invoice());
        final TxnAmount amount = params.amount();
        final String details = params.details();
        if (!params.force()) {
            checkBalance(from, amount);
        }
        final RsaKey rsaKey = new RsaKey(new File(params.privateKey()));
        final Wallet substracted = from.substract(
                amount,
                invoice,
                rsaKey,
                details

        );
        final ParsedTxnData last = new ParsedTxnData(getLast(substracted.transactions()));
        System.out.printf(
                "%d sent from %s to %s\n",
                last.amount().value(),
                from.head().id(),
                last.bnf().asText()
        );
    }

    private static void checkBalance(final Wallet from, final TxnAmount amount) {
        if (amount.lessOrEq(0L)) {
            throw new IllegalArgumentException("Amount can't be negative");
        }
        if (!from.head().id().equals(LongId.ROOT.asText()) && from.balance().less(amount.value())) {
            throw new IllegalStateException(
                    String.format(
                            "Can't send %d Current balance is %d",
                            amount.value(),
                            from.balance().value()
                    )
            );
        }
    }

    /**
     * If invoice doesn't contain @ it means that invoice is recipient wallet id
     * In this case need to get prefix from recipient
     *
     * @param invoice Invoice
     * @return Normalized invoice
     * @throws Exception if failed
     */
    private String normalizedInvoice(final String invoice) throws Exception {
        if (!invoice.contains("@")) {
            return new InvoiceCommand(this.wallets, this.remotes, this.copies).run(new String[]{
                    "-invoice",
                    "receiver=" + invoice
            });
        } else {
            return invoice;
        }
    }
}
