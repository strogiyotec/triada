package io.triada.models.wallet;

import java.io.File;

public interface AllCopy {
    String name();

    File path();

    int total();

    boolean master();

    int score();
}
