package io.triada.models.score;

import io.triada.node.farm.node.NodeData;
import lombok.experimental.UtilityClass;

/**
 * Assertion methods for score
 */
@UtilityClass
public final class AssertScore {

    /**
     * Score valid instance
     */
    private static final IsValidScore scoreValid = new IsValidScore();

    /**
     * Assert using scoreValid
     *
     * @param score To assert
     */
    public void assertValidScore(final Score score) {
        if (!scoreValid.test(score)) {
            throw new AssertionError(
                    String.format(
                            "Invalid score %s",
                            score.asText()
                    )
            );
        }
        if (score.expired(TriadaScore.BEST_BEFORE)) {
            throw new AssertionError(
                    String.format(
                            "Expired score %s with time %s",
                            score.asText(),
                            score.time().toString()
                    )
            );
        }
    }

    /**
     * failed if port or host are different
     *
     * @param score To assert
     * @param data  Data to compare with
     */
    public void assertScoreOwnership(final Score score, final NodeData data) {
        if (!score.address().getHost().equals(data.host())) {
            throw new AssertionError(
                    String.format(
                            "Masqueraded host %s as %s",
                            data.host(),
                            score.address().getHost()
                    )
            );
        }
        if (score.address().getPort() != data.port()) {
            throw new AssertionError(
                    String.format(
                            "Masqueraded port %d as %d",
                            data.port(),
                            score.address().getPort()
                    )
            );
        }
    }

    /**
     * Failed is strength less than STRENGTH
     *
     * @param score to Assert
     */
    public void assertScoreStrength(final Score score) {
        if (score.strength() < TriadaScore.STRENGTH) {
            throw new AssertionError(
                    String.format(
                            "Score %d is too weak",
                            score.strength()
                    )
            );
        }
    }

    /**
     * Failed if score value less than min
     *
     * @param score to Assert
     * @param min   min value of score
     */
    public void assertScoreValue(final Score score, final int min) {
        if (score.value() < min) {
            throw new AssertionError(
                    String.format(
                            "Score %d is too small",
                            score.value()
                    )
            );
        }
    }

}
