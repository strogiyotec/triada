package io.triada.node.farm;

import io.triada.models.score.Score;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.List;

/**
 * This class log preload amount of farmed scores
 */
@UtilityClass
public final class PreloadFarmLog {

    /**
     * @param farm  Farm
     * @param cache File with scores
     * @throws Exception if failed
     */
    public void log(final Farm farm, final File cache) throws Exception {
        final List<Score> best = farm.best();
        if (best.isEmpty()) {
            System.out.printf(
                    "No scores found in the cache at %s \n",
                    cache.toString()
            );
        } else {
            System.out.printf(
                    "%d scores pre-loaded from %s , the best is %s \n",
                    best.size(),
                    cache.toString(),
                    best.get(0).asText()
            );
        }
    }
}
