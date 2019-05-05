package io.triada.commands.taxes;

import com.google.gson.JsonObject;
import io.triada.commands.Command;
import io.triada.commands.remote.Remotes;
import io.triada.models.key.RsaKey;
import io.triada.models.score.AssertScore;
import io.triada.models.score.Score;
import io.triada.models.score.SuffixScore;
import io.triada.models.tax.TaxMetadata;
import io.triada.models.tax.TxnTaxes;
import io.triada.models.transaction.ParsedTxnData;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import io.triada.node.farm.Farm;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static io.triada.http.HttpTriadaClient.READ_TIMEOUT;
import static io.triada.models.tax.TxnTaxes.TRIAL;

/**
 * Command to pay taxes
 */
@AllArgsConstructor
public final class TaxesCommand implements Command {

    private final Wallets wallets;

    private final Remotes remotes;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-taxes")) {
            final TaxesParams taxesParams = new TaxesParams(Arrays.asList(cmd.getOptionValues("taxes")));
            final List<String> ids = taxesParams.wallets();
            if (ids.isEmpty()) {
                System.out.println("At least one node should be provided");
                return;
            }
            if (taxesParams.show()) {
                this.show(ids);
            } else if (taxesParams.debt()) {
                this.debt(ids);
            } else if (taxesParams.pay()) {
                this.pay(ids, taxesParams);
            }
        } else {
            throw new IllegalArgumentException("Need to add taxes option");
        }
    }

    private void pay(final List<String> ids, final TaxesParams params) throws Exception {
        for (final String id : ids) {
            this.pay(id, params);
        }
    }

    private void pay(final String id, final TaxesParams params) throws Exception {
        final Wallet wallet = this.wallets.acq(id);
        TxnTaxes taxes = new TxnTaxes(wallet);
        long debt = taxes.debt();
        System.out.printf(
                "The current debt of %s is %d tridz the balance is %s : %s\n",
                wallet.mnemo(),
                debt,
                wallet.balance().asText(4),
                taxes.asText()
        );
        if (debt < TRIAL.value()) {
            System.out.printf("No need to pay taxes yet , while the debt is less than %d\n", TRIAL.value());
            return;
        }
        final List<Score> top = this.bestScores(params);
        final List<Score> everyBody = new ArrayList<>(top);
        int paid = 0;
        while (debt > TxnTaxes.TRIAL.value()) {
            if (top.isEmpty()) {
                final String message =
                        String.format(
                                "There were %d remote nodes as tax collecting candidates",
                                everyBody.size()
                        );
                if (!params.ignoreNodesAbsence()) {
                    throw new IllegalStateException(message);
                } else {
                    System.out.println(message);
                    break;
                }
            }
            final Score best = top.remove(0);
            if (taxes.exists(TaxMetadata.detais(best))) {
                System.out.printf("The score has already been taxes : %s\n", best.asText());
                continue;
            }
            taxes = taxes.pay(
                    new RsaKey(new File(params.privateKey())),
                    best
            );
            final ParsedTxnData data = new ParsedTxnData(taxes.last().get());
            debt += data.amount().value();
            paid++;
            System.out.printf(
                    "%d of taxes paid from %s to %s Payment number %d ,txn %d/%d left to pay\n",
                    data.amount().value(),
                    wallet.head().id(),
                    data.bnf().asText(),
                    paid,
                    data.id(),
                    wallet.transactions().size()
            );
        }

    }

    private void debt(final List<String> ids) throws Exception {
        for (final String id : ids) {
            this.debt(id);
        }
    }

    private void debt(final String id) throws Exception {
        final TxnTaxes txnTaxes = new TxnTaxes(this.wallets.acq(id));
        System.out.println(txnTaxes.debt());
        System.out.println(txnTaxes.asText());
    }

    private void show(final List<String> ids) throws Exception {
        for (final String id : ids) {
            this.show(id);
        }
    }

    private void show(final String id) throws Exception {
        System.out.println(new TxnTaxes(this.wallets.acq(id)).asText());
    }

    private List<Score> bestScores(final TaxesParams params) throws Exception {
        final List<Score> best = new ArrayList<>(16);
        this.remotes.modify(
                remoteNode -> {
                    final JsonObject jsonObject = remoteNode.http("").get(READ_TIMEOUT);
                    final SuffixScore score = new SuffixScore(jsonObject.get("score").getAsJsonObject());
                    AssertScore.assertValidScore(score);
                    AssertScore.assertScoreOwnership(score, remoteNode.address());
                    if (!params.ignoreScoreWeakness()) {
                        AssertScore.assertScoreStrength(score);
                    }
                    best.add(score);
                },
                Farm.EMPTY);
        best.sort(Comparator.comparing(Score::value).reversed());
        return best;
    }
}
