package io.triada.commands.calculate;

import com.google.common.net.HostAndPort;
import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.Arrays;

/**
 * Calculate score
 */
public final class CalculateCommand implements ValuableCommand<Score> {
    @Override
    public Score run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-calculate")) {
            final CalculateParams calculateParams = new CalculateParams(Arrays.asList(cmd.getOptionValues("calculate")));
            return this.calculate(calculateParams);
        } else {
            throw new IllegalArgumentException("Need to add calculate option");
        }
    }

    /**
     * Calculate suffixes until reach max number provided from cli
     *
     * @param params Cli params
     * @return Score
     */
    private Score calculate(final CalculateParams params) {
        final int strength = params.strength();
        final long start = System.currentTimeMillis();
        if (strength < 0 || strength > 8) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid strength :%d",
                            strength
                    )
            );
        }
        Score score = new TriadaScore(
                params.time(),
                HostAndPort.fromParts(params.host(), params.port()),
                params.invoice(),
                strength
        );
        final int max = params.max();
        while (score.value() < max) {
            final StringBuilder msg = new StringBuilder(score.asText());
            if (!params.hideHash()) {
                msg.append(score.value() > 0 ? " " + score.hash() : " ");
            }
            if (!params.hideTime()) {
                msg.append(System.currentTimeMillis() - start).append("s");
            }
            System.out.println(msg);
            score = score.next();
        }
        System.out.printf("Took %d seconds to find Score\n", System.currentTimeMillis() - start);
        return score;
    }
}
