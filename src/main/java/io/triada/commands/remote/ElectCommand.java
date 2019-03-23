package io.triada.commands.remote;

import io.triada.commands.Command;
import io.triada.commands.ValuableCommand;
import io.triada.http.HttpTriadaClient;
import io.triada.models.score.AssertScore;
import io.triada.models.score.Score;
import io.triada.models.score.SuffixScore;
import io.triada.node.farm.Farm;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Return list of scores from given Remotes
 */
@AllArgsConstructor
public final class ElectCommand implements ValuableCommand<List<Score>> {

    /**
     * Remotes
     */
    private final Remotes remotes;

    /**
     * Farm
     */
    private final Farm farm;

    @Override
    public List<Score> run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-r_elect")) {
            return this.elect(cmd);
        } else {
            throw new IllegalStateException("Command -r_elect was not provided");
        }
    }

    private List<Score> elect(final CommandLine commandLine) throws Exception {
        final boolean ignoreScoreWeek = commandLine.hasOption("-ignore_score_weakness");
        final String maxWinners = commandLine.getOptionValue("max-winners");
        final List<Score> scores = new ArrayList<>(16);
        this.remotes.modify(remoteNode -> {
            final SuffixScore score = new SuffixScore(
                    remoteNode.http("")
                            .get(HttpTriadaClient.READ_TIMEOUT)
                            .get("score")
                            .getAsJsonObject()
            );
            AssertScore.assertValidScore(score);
            AssertScore.assertScoreOwnership(score, remoteNode.address());
            if (!ignoreScoreWeek) {
                AssertScore.assertScoreStrength(score);
                AssertScore.assertScoreValue(score, Integer.parseInt(commandLine.getOptionValue("min-score")));
            }
            if (commandLine.hasOption("-ignore-master") && remoteNode.isMaster()) {
                System.out.printf("RemoteNode %s was ignored because it master", remoteNode.asText());
            } else {
                scores.add(score);
            }
        }, this.farm);
        return scores.stream()
                .peek(score -> System.out.println("Elected: " + score.asText()))
                .sorted(Comparator.comparingInt(Score::value).reversed())
                .limit(maxWinners == null ? 1 : Integer.parseInt(maxWinners))
                .collect(Collectors.toList());
    }
}
