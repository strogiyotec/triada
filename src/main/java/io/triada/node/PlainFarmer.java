package io.triada.node;

import io.triada.models.score.Score;

/**
 * Simple farmer that return next score
 */
public final class PlainFarmer implements Farms {
    @Override
    public Score up(final Score score) {
        return score.next();
    }
}
