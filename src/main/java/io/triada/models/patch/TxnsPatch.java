package io.triada.models.patch;

import io.triada.models.transaction.SignedTransaction;
import io.triada.models.wallet.Wallet;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@AllArgsConstructor
public final class TxnsPatch implements Patch {

    private final Wallet wallet;

    private final List<SignedTransaction> txns = new ArrayList<>(16);

    @Override
    public void legacy(final Wallet wallet, final int hours) {

    }

    @Override
    public void join(final Wallet wallet, final File ledger, final Callable<Boolean> yeild) {

    }

    @Override
    public boolean empty() {
        return false;
    }

    @Override
    public boolean save() {
        return false;
    }

    @Override
    public String asText() {
        if (this.txns.isEmpty()) {
            return "nothing";
        } else {
            return this.txns.size() + " txns";
        }
    }
}
