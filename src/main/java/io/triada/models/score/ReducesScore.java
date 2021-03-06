package io.triada.models.score;

import java.util.List;

/**
 * Return new SuffixScore with reduced amount of suffixes
 */
public final class ReducesScore extends ScoreEnvelope {

    /**
     * @param amount Of suffixes
     * @param origin Origin score
     */
    public ReducesScore(final int amount, final Score origin) {
        super(() -> {
            final List<String> suffixes = origin.suffixes();
            if (suffixes.isEmpty()) {
                throw new IllegalArgumentException("Can't reduce empty Score");
            }
            final int min = Math.min(amount, suffixes.size());
            return new SuffixScore(
                    origin.time(),
                    origin.address(),
                    origin.invoice(),
                    suffixes.subList(0, min),
                    origin.strength(),
                    origin.createdAt()
            );
        });
    }

    /**
     * Default amount is 4
     *
     * @param origin Origin score
     */
    public ReducesScore(final Score origin) {
        this(4, origin);
    }
}
