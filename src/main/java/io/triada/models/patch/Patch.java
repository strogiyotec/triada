package io.triada.models.patch;

import io.triada.models.wallet.Wallet;
import io.triada.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public interface Patch extends Text {

    void legacy(Wallet wallet, int hours);

    void join(Wallet wallet, File ledger, Callable<Boolean> yeild) throws Exception;

    boolean empty();

    boolean save(File file, boolean overwrite) throws IOException;

    default void legacy(Wallet wallet) {
        this.legacy(wallet, 24);
    }

    default boolean save(File file) throws IOException {
        return this.save(file, false);
    }

    default void join(Wallet wallet, Callable<Boolean> yeild) throws Exception {
        this.join(wallet, new File("/dev/null"), yeild);
    }
}
