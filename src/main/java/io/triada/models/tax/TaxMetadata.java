package io.triada.models.tax;

import io.triada.models.score.ReducesScore;
import io.triada.models.score.Score;
import lombok.experimental.UtilityClass;

/**
 * Add all methods which don't need a state
 */
@UtilityClass
public final class TaxMetadata {

    public String detais(final Score score) {
        return String.join(" ", TxnTaxes.PREFIX, new ReducesScore(score).asText());
    }
}
