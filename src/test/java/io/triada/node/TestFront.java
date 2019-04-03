package io.triada.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import io.triada.Triada;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeFile;
import io.triada.mocks.FakeHome;
import io.triada.models.id.LongId;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import io.triada.node.farm.AsyncFarm;
import io.triada.node.farm.SingleThreadScoreFarm;
import io.triada.node.front.FrontPage;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.hamcrest.Matchers;
import org.jooq.lambda.Unchecked;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TestFront extends Assert {

    @ClassRule
    public static final TemporaryFolder folder = new TemporaryFolder();

    private static final FakeHome fakeHome = new FakeHome();

    private static final Wallet wallet = Unchecked.supplier(() -> fakeHome.createEagerWallet(new LongId(12345L))).get();

    private static ExecutorService service = Executors.newSingleThreadExecutor();

    private static FrontPage frontPage;

    private static final String INVOICE = "NOPREFIX@ffffffffffffffff";

    private final RestTemplate template = new RestTemplate();

    @BeforeClass
    public static void start() throws Throwable {
        final AsyncFarm farm = new AsyncFarm(
                new SingleThreadScoreFarm(
                        new FakeFile(TriadaWallet.EXT).call(),
                        2,
                        INVOICE
                ),
                service
        );
        final File ledger = folder.newFile("ledger.csv");

        frontPage = new FrontPage(
                ImmutableMap.of(
                        "protocol", Triada.TEST_NETWORK,
                        "version", Triada.VERSION
                ),
                farm,
                Unchecked.supplier(() -> folder.newFile("ledger.csv")).get(),
                new Wallets(wallet.file().getParentFile()),
                new RemoteNodes(Unchecked.supplier(() -> folder.newFolder("remotes")).get()),
                8080
        );
        farm.start(HostAndPort.fromParts("localhost", 8080), () -> Thread.sleep(30000));
        final VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(1000 * 60 * 60);
        final Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(frontPage);
        Thread.sleep(2000);//need some time to deploy verticle
    }

    @Test
    public void testStaticRoutes() throws Exception {
        final ResponseEntity<String> protocol = template.getForEntity("http://localhost:8080/protocol", String.class);
        final ResponseEntity<String> version = template.getForEntity("http://localhost:8080/version", String.class);
        final ResponseEntity<String> pid = template.getForEntity("http://localhost:8080/pid", String.class);
        final ResponseEntity<String> wallets = template.getForEntity("http://localhost:8080/wallets", String.class);
        final ResponseEntity<String> ledger = template.getForEntity("http://localhost:8080/ledger", String.class);

        assertEquals(Triada.TEST_NETWORK, protocol.getBody());
        assertEquals(Triada.VERSION, version.getBody());
        assertTrue(pid.hasBody());
        assertEquals(wallets.getBody(), "0000000000003039");// 12345L in hex
        assertTrue(!ledger.hasBody());
    }

    @Test
    public void testDynamicRoutes() throws Exception {
        final ResponseEntity<String> farm = template.getForEntity("http://localhost:8080/farm", String.class);
        final ResponseEntity<String> score = template.getForEntity("http://localhost:8080/score", String.class);

        assertThat(farm.getBody(), Matchers.containsString("best"));
        assertThat(score.getBody(), Matchers.containsString(INVOICE));

    }

    @AfterClass
    public static void close() throws Exception {
        frontPage.close();
        service.shutdownNow();
    }
}
