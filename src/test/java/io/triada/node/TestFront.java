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
import io.triada.node.entrance.BlockingEntrance;
import io.triada.node.farm.AsyncFarm;
import io.triada.node.farm.SingleThreadScoreFarm;
import io.triada.node.front.FrontPage;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hamcrest.Matchers;
import org.jooq.lambda.Unchecked;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;

public final class TestFront extends Assert {

    @ClassRule
    public static final TemporaryFolder folder = new TemporaryFolder();

    private static final FakeHome fakeHome = new FakeHome();

    private static final Wallet wallet = Unchecked.supplier(() -> fakeHome.createEagerWallet(new LongId(12345L))).get();

    private static ExecutorService service = Executors.newSingleThreadExecutor();

    private static FrontPage frontPage;

    private static final String INVOICE = "NOPREFIX@ffffffffffffffff";

    private final RestTemplate template = new RestTemplate();

    /**
     * init in before class
     */
    private static String walletId;

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
        final RemoteNodes nodes = new RemoteNodes(Unchecked.supplier(() -> folder.newFile("remotes")).get());
        nodes.add("localhost", 22);
        nodes.add("localhost", 44);

        final Wallets wallets = new Wallets(wallet.file().getParentFile());

        frontPage = new FrontPage(
                ImmutableMap.of(
                        "protocol", Triada.TEST_NETWORK,
                        "version", Triada.VERSION
                ),
                ImmutableMap.of("disable-push", false),
                farm,
                ledger,
                wallets,
                nodes,
                8080,
                new BlockingEntrance(
                        wallets,
                        nodes,
                        wallet.file().getParentFile().toPath(),
                        Triada.TEST_NETWORK,
                        ledger.toPath()
                )
        );
        farm.start(HostAndPort.fromParts("localhost", 8080), () -> Thread.sleep(30000));
        final VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(1000 * 60 * 60);
        final Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(frontPage);
        Thread.sleep(2000);//need some time to deploy verticle

        walletId = wallet.head().id();
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
    public void testRemotes() throws Exception {
        Thread.sleep(5000);//need some time to find suffixes
        final ResponseEntity<String> remotes = template.getForEntity("http://localhost:8080/remotes", String.class);
        final JsonObject remoteNodes = new JsonObject(remotes.getBody());
        assertTrue(remoteNodes.containsKey("remotes"));

        final JsonArray remotesJA = remoteNodes.getJsonArray("remotes");
        assertThat(remotesJA.getString(0), Matchers.containsString("22"));
        assertThat(remotesJA.getString(1), Matchers.containsString("44"));
    }

    @Test
    public void testGetWallet() throws Exception {
        final ResponseEntity<String> response = template.getForEntity("http://localhost:8080/wallet/" + walletId, String.class);
        final JsonObject body = new JsonObject(response.getBody());

        assertThat(body.getString("protocol"), is(Triada.TEST_NETWORK));
        assertThat(body.getString("version"), is(Triada.VERSION));
        assertThat(body.getJsonObject("score").getString("host"), is("localhost"));
        assertThat(body.getJsonObject("score").getInteger("port"), is(8080));
        assertThat(body.getInteger("taxes"), is(0));
        assertThat(body.getInteger("debt"), is(0));
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
