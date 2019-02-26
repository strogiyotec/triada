package io.triada.models.wallet;

import lombok.AllArgsConstructor;

import java.io.File;

@AllArgsConstructor
public final class ConstAllCopy implements AllCopy {

    private final String name;

    private final File path;

    private final int total;

    private final boolean master;

    private final int score;

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public File path() {
        return this.path;
    }

    @Override
    public int total() {
        return this.total;
    }

    @Override
    public boolean master() {
        return this.master;
    }

    @Override
    public int score() {
        return this.score;
    }
}
