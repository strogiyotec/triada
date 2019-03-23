package io.triada.models;

import com.google.common.net.HostAndPort;
import io.triada.models.score.IsValidScore;
import io.triada.models.score.ReducesScore;
import io.triada.models.score.Score;
import io.triada.models.score.SuffixScore;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
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
                new SuffixScore(
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
        final SuffixScore score = new SuffixScore(
                new Date(System.currentTimeMillis() - (1000 * 60 * 60 * SuffixScore.BEST_BEFORE)),
                HOST_AND_PORT,
                INVOICE,
                50,
                new Date()
        );

        assertTrue(this.isValidScore.test(score));
        assertTrue(!score.expired(SuffixScore.BEST_BEFORE));
        assertEquals(0, score.value());
    }

    @Test
    public void testWrongScore() {
        final ZonedDateTime zp = ZonedDateTime.parse("2017-07-19T21:24:51Z");
        final SuffixScore score = new SuffixScore(
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
        final Score score = new SuffixScore(
                new Date(),
                HOST_AND_PORT,
                INVOICE,
                2,
                new Date()
        ).next().next().next();

        assertEquals(3, score.value());
        assertTrue(this.isValidScore.test(score));
        assertTrue(!score.expired(SuffixScore.BEST_BEFORE));
    }

    @Test
    public void testFutureTimeNotValid() {
        final Score score = new SuffixScore(
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
        final Score score = new SuffixScore(
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
        final SuffixScore suffixScore = new SuffixScore("3 1548869681 localhost 8080 NOPREFIX@ffffffffffffffff");

        assertEquals(suffixScore.strength(), 3);
        assertEquals(suffixScore.time(), new Date(1548869681));
        assertEquals(suffixScore.address(), HostAndPort.fromParts("localhost", 8080));
        assertEquals(suffixScore.invoice(), INVOICE);
        assertTrue(suffixScore.suffixes().isEmpty());
    }

    @Test
    public void testAsTextFromParsed() {
        final SuffixScore score1 = new SuffixScore("3 1548869681 localhost 8080 NOPREFIX@ffffffffffffffff");
        final SuffixScore score2 = new SuffixScore(score1.asText());

        assertEquals("3 1548869681 localhost 8080 NOPREFIX@ffffffffffffffff", score1.asText().trim());
        assertEquals(score1.asText(), score2.asText());
    }

    @Test
    public void testParseSuffixScore() {
        final SuffixScore suffixScore = new SuffixScore("3 1548869681 localhost 8080 NOPREFIX@ffffffffffffffff AF_FD");

        assertEquals(suffixScore.strength(), 3);
        assertEquals(suffixScore.time(), new Date(1548869681));
        assertEquals(suffixScore.address(), HostAndPort.fromParts("localhost", 8080));
        assertEquals(suffixScore.invoice(), INVOICE);
        assertEquals(suffixScore.suffixes(), Arrays.asList("AF", "FD"));
    }

    @Test
    public void testCalculateSuffixForParsedScore() {
        final String line = "TAXES 6 1549094153600 b2.zold.io 1000 DCexx0hG@912ecc24b32dbe74 52310229_24729451_14470076_5837578_49671844_22449904_1972513_4434596";
        final String[] split = line.split(" ", 2);
        final SuffixScore suffixScore = new SuffixScore(split[1]);
        assertEquals(suffixScore.value(), 8);
        assertTrue(isValidScore.test(suffixScore));
    }

    @Test
    public void testPrintMnemo() throws Exception {
        final SuffixScore score = new SuffixScore(
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2017-07-19T22:32:51"),
                HostAndPort.fromParts("localhost", 80),
                "NOPREFIX@ffffffffffffffff",
                SuffixScore.STRENGTH
        );

        assertEquals(score.mnemo(), "0:2232");
    }


}
