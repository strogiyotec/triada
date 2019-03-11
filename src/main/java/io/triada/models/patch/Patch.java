package io.triada.models.patch;

import io.triada.functions.CheckedToBooleanFunction;
import io.triada.models.transaction.SignedTransaction;
import io.triada.models.wallet.Wallet;
import io.triada.text.Text;
import org.jooq.lambda.fi.util.function.CheckedFunction;

import java.io.File;
import java.io.IOException;

public interface Patch extends Text {

    void legacy(Wallet wallet, int hours);

    void join(Wallet wallet, File ledger, CheckedToBooleanFunction<SignedTransaction> yield) throws Exception;

    boolean empty();

    boolean save(File file, boolean overwrite) throws IOException;

    default void legacy(Wallet wallet) {
        this.legacy(wallet, 24);
    }

    default boolean save(File file) throws IOException {
        return this.save(file, false);
    }

    default void join(Wallet wallet, CheckedToBooleanFunction<SignedTransaction> yield) throws Exception {
        this.join(wallet, new File("/dev/null"), yield);
    }

}
