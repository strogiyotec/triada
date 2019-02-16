package io.triada.models.score;

import com.google.common.net.HostAndPort;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

@AllArgsConstructor
public abstract class ScoreEnvelope implements Score {

    private final Score origin;

    ScoreEnvelope(final Supplier<Score> supplier) {
        this.origin = supplier.get();
    }

    @Override
    public final Score next() {
        return this.origin.next();
    }

    @Override
    public final List<String> suffixes() {
        return this.origin.suffixes();
    }

    @Override
    public final Date createdAt() {
        return this.origin.createdAt();
    }

    @Override
    public final HostAndPort address() {
        return this.origin.address();
    }

    @Override
    public final String invoice() {
        return this.origin.invoice();
    }

    @Override
    public final int strength() {
        return this.origin.strength();
    }

    @Override
    public final Date time() {
        return this.origin.time();
    }

    @Override
    public final String hash() {
        return this.origin.hash();
    }

    @Override
    public final int value() {
        return this.origin.value();
    }

    @Override
    public final boolean expired(final int hours) {
        return this.origin.expired(hours);
    }

    @Override
    public final String asText() {
        return this.origin.asText();
    }

    @Override
    public final String mnemo() {
        return this.origin.mnemo();
    }

    @Override
    public final boolean valid() {
        return this.origin.valid();
    }
}
