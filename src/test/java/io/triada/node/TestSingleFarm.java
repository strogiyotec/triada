package io.triada.node;

import com.google.common.net.HostAndPort;
import io.triada.mocks.FakeFile;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import io.triada.models.wallet.TriadaWallet;
import io.triada.node.farm.Farm;
import io.triada.node.farm.SingleThreadScoreFarm;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Test single thread farm
 */
public final class TestSingleFarm extends Assert {

    @Test
    public void testCorrectScoreFromEmptyFarm() throws Throwable {
        final Farm farm = new SingleThreadScoreFarm(
                new FakeFile(TriadaWallet.EXT).call(),
                1,
                "NOPREFIX6@ffffffffffffffff"
        );
        farm.start(HostAndPort.fromParts("localhost", 8080), () -> {
            final List<Score> best = farm.best();
            assertTrue(!best.isEmpty());
            final Score score = best.get(0);
            assertTrue(!score.expired(TriadaScore.BEST_BEFORE));
            assertEquals(0, score.value());
            assertEquals("localhost", score.address().getHost());
            assertEquals(8080, score.address().getPort());
        });
    }

    /**
     * Should farm at list 2 suffixes
     *
     * @throws Throwable if failed
     */
    @Test
    public void testMakedBestScore() throws Throwable {
        final Farm farm = new SingleThreadScoreFarm(
                new FakeFile(TriadaWallet.EXT).call(),
                1,
                "NOPREFIX7@ffffffffffffffff"
        );
        farm.start(HostAndPort.fromParts("localhost", 8081), () -> {
            TimeUnit.SECONDS.sleep(20);
            final List<Score> best = farm.best();
            assertTrue(!best.isEmpty());
            assertTrue(best.get(0).value() > 2);
        });
    }
}
