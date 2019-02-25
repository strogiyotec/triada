package io.triada.models.wallet;

import lombok.AllArgsConstructor;

import java.util.Date;

@AllArgsConstructor
public final class ConstCsvCopy implements CsvCopy {

    private final String name;

    private final String host;

    private final int port;

    private final int score;

    private final Date date;

    private final boolean master;

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String host() {
        return this.host;
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public int score() {
        return this.score;
    }

    @Override
    public Date time() {
        return this.date;
    }

    @Override
    public boolean master() {
        return this.master;
    }

    @Override
    public String asText() {
        return String.format(
                "%s,%s,%d,%d,%d,%s\n",
                this.name,
                this.host,
                this.port,
                this.score,
                this.time().getTime(),
                this.master ? "M" : "E"
        );
    }
}
