package io.triada.models.score;

import java.util.Collections;
import java.util.Date;
import java.util.function.Predicate;

public final class IsValidScore implements Predicate<Score> {
    @Override
    public boolean test(final Score score) {
        return
                (score.suffixes().isEmpty() || score.hash().endsWith(String.join("", Collections.nCopies(score.strength(), "0"))))
                        && score.time().compareTo(new Date()) < 0;
    }
}
