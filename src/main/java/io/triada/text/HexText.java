package io.triada.text;

import lombok.experimental.Delegate;

import java.util.Objects;

/**
 * Create hex text from given number
 */
public final class HexText implements CharSequence {

    @Delegate
    private final String origin;

    public HexText(final long origin) {
        this.origin = Long.toString(origin, 16);
    }

    @Override
    public String toString() {
        return this.origin;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        return o != null && o instanceof CharSequence && this.origin.equals(o.toString());
    }

    @Override
    public int hashCode() {

        return Objects.hash(origin);
    }
}
