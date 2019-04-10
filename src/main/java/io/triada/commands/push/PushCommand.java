package io.triada.commands.push;

import com.google.gson.JsonObject;
import io.triada.commands.Command;
import io.triada.commands.remote.RemoteNode;
import io.triada.commands.remote.Remotes;
import io.triada.models.score.AssertScore;
import io.triada.models.score.Score;
import io.triada.models.score.SuffixScore;
import io.triada.models.tax.TxnTaxes;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallet;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: 4/10/19 Add test
@AllArgsConstructor
public final class PushCommand implements Command {

    private final EagerWallets wallets;

    private final Remotes remotes;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-push")) {
            final PushParams params = new PushParams(Arrays.asList(cmd.getOptionValues("push")));
            for (final String id : params.ids()) {
                this.push(id, params);
            }
        } else {
            throw new IllegalArgumentException("Need to add push option");
        }
    }

    private void push(final String id, final PushParams params) throws Exception {
        if (this.remotes.all().isEmpty()) {
            throw new IllegalStateException("No remotes");
        }
        final Wallet wallet = this.wallets.acq(id);
        if (!wallet.file().exists()) {
            throw new IllegalStateException("Wallet file doesn't exist");
        }
        if (new TxnTaxes(wallet).debt() > TxnTaxes.TRIAL.value()) {
            System.out.printf("Taxes %s are not paid , most likely the wallet won't be accepted", id);
        }
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger masters = new AtomicInteger(0);
        final AtomicInteger nodes = new AtomicInteger(0);
        this.remotes.modify(remoteNode -> {
            total.getAndAdd(this.pushOne(id, remoteNode, params));
            nodes.incrementAndGet();
            if (remoteNode.isMaster()) {
                masters.incrementAndGet();
            }
        });
        if (masters.get() == 0 && !params.tolerateEdges()) {
            throw new IllegalStateException("No masternodes in given remotes");
        }
        if (nodes.get() < params.tolerateQuorum()) {
            throw new IllegalStateException(
                    String.format(
                            "There were not enough nodes , expected %d , given %d",
                            params.tolerateQuorum(),
                            nodes.get()
                    )
            );
        }
        System.out.println("Push finished");
    }

    // TODO: 4/10/19 Add ignore node logic
    private int pushOne(final String id, final RemoteNode remoteNode, final PushParams params) {
        try {
            final Score response = this.readOne(id, remoteNode, params);
            AssertScore.assertValidScore(response);
            AssertScore.assertScoreOwnership(response, remoteNode.address());
            if (!params.ignoreScoreWeekness()) {
                AssertScore.assertScoreStrength(response);
            }
            return response.value();
        } catch (final Exception exc) {
            return 0;
        }
    }

    // TODO: 4/10/19 Retry logic
    private Score readOne(final String id, final RemoteNode remoteNode, final PushParams params) throws Exception {
        final String url = String.format("/wallet/%s", id);
        final JsonObject jsonObject = remoteNode.http(url).putFile(this.wallets.acq(id).file());
        return new SuffixScore(jsonObject.get("score").getAsString());
    }
}
