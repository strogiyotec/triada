package io.triada.node.farm;

import io.triada.models.cli.ShellScript;
import io.triada.models.score.TriadaScore;
import io.triada.models.wallet.TriadaWallet;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class TestFarm extends Assert {

    @Test
    public void testRenderJson() throws Exception {
        final File cache = File.createTempFile("/tmp/", TriadaWallet.EXT);
        cache.deleteOnExit();
        final ScoreFarm scoreFarm = new ScoreFarm(
                cache,
                "NOPREFIX6@ffffffffffffffff",
                new ThreadPoolExecutor(5, 5, 1000L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5)),
                24 * 60 * 60,
                TriadaScore.STRENGTH,
                new ShellScript(),
                new PlainFarmer()
        );

        assertNotNull(scoreFarm.asJson());
    }
}
