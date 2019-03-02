package io.triada.models.wallet;

import java.io.File;

public interface WalletCopy {
    String name();

    File path();

    int total();

    boolean master();

    int score();
}
