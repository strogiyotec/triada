package io.triada.models.patch;

import io.triada.models.wallet.Wallet;
import io.triada.text.Text;

import java.io.File;
import java.util.concurrent.Callable;

public interface Patch extends Text {

    void legacy(Wallet wallet, int hours);

    void join(Wallet wallet, File ledger, Callable<Boolean> yeild);

    boolean empty();

    boolean save();
}
