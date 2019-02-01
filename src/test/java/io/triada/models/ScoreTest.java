package io.triada.models;

import com.google.common.net.HostAndPort;
import io.triada.models.score.IsValidScore;
import io.triada.models.score.ReducesScore;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;

public final class ScoreTest extends Assert {

    private final IsValidScore isValidScore = new IsValidScore();

    private static final HostAndPort HOST_AND_PORT = HostAndPort.fromParts("localhost", 8888);

    private static final String INVOICE = "NOPREFIX@ffffffffffffffff";

    @Test
    public void testReduceItself() {
        final ZonedDateTime zp = ZonedDateTime.parse("2017-07-19T21:24:51Z");
        final int reduces = 2;
        final ReducesScore reducesScore = new ReducesScore(
                reduces,
                new TriadaScore(
                        Date.from(zp.toInstant()),
                        HOST_AND_PORT,
                        INVOICE,
                        Arrays.asList("A", "B", "C", "D", "E", "F", "G"),
                        3,
                        new Date()
                )
        );
        assertEquals(2, reducesScore.value());
        assertEquals(64, reducesScore.hash().length());
    }

    @Test
    public void testZeroSuffixes() {
        final TriadaScore score = new TriadaScore(
                new Date(System.currentTimeMillis() - (1000 * 60 * 60 * TriadaScore.BEST_BEFORE)),
                HOST_AND_PORT,
                INVOICE,
                50,
                new Date()
        );

        assertTrue(this.isValidScore.test(score));
        assertTrue(!score.expired(TriadaScore.BEST_BEFORE));
        assertEquals(0, score.value());
    }

    @Test
    public void testWrongScore() {
        final ZonedDateTime zp = ZonedDateTime.parse("2017-07-19T21:24:51Z");
        final TriadaScore score = new TriadaScore(
                Date.from(zp.toInstant()),
                HOST_AND_PORT,
                INVOICE,
                Arrays.asList("xxx", "yyy", "zzz"),
                new Date()
        );

        assertEquals(3, score.value());
        assertFalse(isValidScore.test(score));
    }

    @Test
    public void testFindNextScore() {
        final Score score = new TriadaScore(
                new Date(),
                HOST_AND_PORT,
                INVOICE,
                2,
                new Date()
        ).next().next().next();

        assertEquals(3, score.value());
        assertTrue(this.isValidScore.test(score));
        assertTrue(!score.expired(TriadaScore.BEST_BEFORE));
    }

    @Test
    public void testFutureTimeNotValid() {
        final Score score = new TriadaScore(
                new Date(System.currentTimeMillis() + 60 * 60),
                HOST_AND_PORT,
                INVOICE,
                2,
                new Date()
        );
        assertFalse(this.isValidScore.test(score));
    }

    @Test
    public void testCorrectAmountOfZeroes() {
        final Score score = new TriadaScore(
                new Date(),
                HOST_AND_PORT,
                INVOICE,
                4,
                new Date()
        ).next();
        assertTrue(score.hash().endsWith("0000"));
    }

    @Test
    public void testParseNoSuffixScore() {
        final TriadaScore triadaScore = new TriadaScore("3 1548869681 localhost 8080 NOPREFIX@ffffffffffffffff");

        assertEquals(triadaScore.strength(), 3);
        assertEquals(triadaScore.time(), new Date(1548869681));
        assertEquals(triadaScore.address(), HostAndPort.fromParts("localhost", 8080));
        assertEquals(triadaScore.invoice(), INVOICE);
        assertTrue(triadaScore.suffixes().isEmpty());
    }

    @Test
    public void testAsTextFromParsed() {
        final TriadaScore score1 = new TriadaScore("3 1548869681 localhost 8080 NOPREFIX@ffffffffffffffff");
        final TriadaScore score2 = new TriadaScore(score1.asText());

        assertEquals("3 1548869681 localhost 8080 NOPREFIX@ffffffffffffffff",score1.asText().trim());
        assertEquals(score1.asText(),score2.asText());
    }

    @Test
    public void testParseSuffixScore() {
        final TriadaScore triadaScore = new TriadaScore("3 1548869681 localhost 8080 NOPREFIX@ffffffffffffffff AF_FD");

        assertEquals(triadaScore.strength(), 3);
        assertEquals(triadaScore.time(), new Date(1548869681));
        assertEquals(triadaScore.address(), HostAndPort.fromParts("localhost", 8080));
        assertEquals(triadaScore.invoice(), INVOICE);
        assertEquals(triadaScore.suffixes(), Arrays.asList("AF", "FD"));
    }

}
