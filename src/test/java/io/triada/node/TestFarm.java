package io.triada.node;

import com.google.common.net.HostAndPort;
import io.triada.mocks.FakeFile;
import io.triada.models.cli.ShellScript;
import io.triada.models.score.SuffixScore;
import io.triada.models.wallet.TriadaWallet;
import io.triada.node.farm.PlainFarmer;
import io.triada.node.farm.ScoreFarm;
import io.triada.threads.NamedThreadExecutor;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class TestFarm extends Assert {

    @Test
    public void testRenderJson() throws Exception {
        final File cache = new FakeFile(TriadaWallet.EXT).call();
        final ScoreFarm scoreFarm = new ScoreFarm(
                cache,
                "NOPREFIX6@ffffffffffffffff",
                new NamedThreadExecutor(
                        new ThreadPoolExecutor(5, 5, 1000L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5))
                ),
                24 * 60 * 60,
                SuffixScore.STRENGTH,
                new ShellScript(),
                new PlainFarmer()
        );

        assertNotNull(scoreFarm.asJson());
    }

    @Test
    public void testMakesManyScores() throws Throwable {
        final ScoreFarm scoreFarm = new ScoreFarm(
                new FakeFile(TriadaWallet.EXT).call(),
                "NOPREFIX7@ffffffffffffffff",
                new NamedThreadExecutor(
                        new ThreadPoolExecutor(
                                5,
                                5,
                                1000L,
                                TimeUnit.SECONDS,
                                new ArrayBlockingQueue<>(5)
                        )
                ),
                new PlainFarmer(),
                1
        );
        scoreFarm.start(HostAndPort.fromParts("localhost", 4567), () -> {
            try {
                Thread.sleep(30000);
            } catch (final InterruptedException e) {
                throw new RuntimeException("Main thread can't be interrupted");
            }
        });
        assertTrue(scoreFarm.best().size() == 4);
    }
}
