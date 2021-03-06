package io.triada.models.score;

import com.google.common.net.HostAndPort;
import lombok.experimental.UtilityClass;

/**
 * Assertion methods for score
 */
@UtilityClass
public final class AssertScore {

    /**
     * Assert using scoreValid
     *
     * @param score To assert
     */
    public void assertValidScore(final Score score) {
        if (!score.valid()) {
            throw new AssertionError(
                    String.format(
                            "Invalid score %s",
                            score.asText()
                    )
            );
        }
        if (score.expired(SuffixScore.BEST_BEFORE)) {
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
     * @param score       To assert
     * @param hostAndPort Data to compare with
     */
    public void assertScoreOwnership(final Score score, final HostAndPort hostAndPort) {
        if (!score.address().getHost().equals(hostAndPort.getHost())) {
            throw new AssertionError(
                    String.format(
                            "Masqueraded host %s as %s",
                            hostAndPort.getHost(),
                            score.address().getHost()
                    )
            );
        }
        if (score.address().getPort() != hostAndPort.getPort()) {
            throw new AssertionError(
                    String.format(
                            "Masqueraded port %d as %d",
                            hostAndPort.getPort(),
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
        if (score.strength() < SuffixScore.STRENGTH) {
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
