package io.triada.routine;

import io.triada.commands.remove.RemoveCommand;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallet;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class WalletGc implements Scheduled {

    private final GcParams params;

    private final EagerWallets wallets;

    public WalletGc(final List<String> params, final EagerWallets wallets) {
        this.params = new GcParams(params);
        this.wallets = wallets;
    }

    @Override
    public void run() throws Exception {
        if (!this.params.routineImmediately()) {
            TimeUnit.SECONDS.sleep(60);
        }
        final RemoveCommand cmd = new RemoveCommand(this.wallets.dir());
        int seen = 0;
        int removed = 0;
        for (final String id : this.wallets.all()) {
            seen++;
            final Wallet wallet = this.wallets.acq(id);
            final boolean needDelete =
                    wallet.file().exists() &&
                            new Date(
                                    wallet.file().lastModified()
                            ).compareTo(
                                    new Date(System.currentTimeMillis() - this.params.gcAge())
                            ) < 0 &&
                            wallet.transactions().isEmpty();
            if (needDelete) {
                cmd.run(
                        new String[]{
                                "-remove",
                                wallet.head().id()
                        }
                );
                removed++;
            }
        }
        System.out.printf(
                "Removed %d empty+old wallets out of %d total\n",
                removed,
                seen
        );

    }
}
