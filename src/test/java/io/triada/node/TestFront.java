package io.triada.node;

import com.google.common.collect.ImmutableMap;
import io.triada.Triada;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import io.triada.node.farm.SingleThreadScoreFarm;
import io.triada.node.front.FrontPage;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.jooq.lambda.Unchecked;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class TestFront extends Assert {

    @ClassRule
    public static final TemporaryFolder folder = new TemporaryFolder();

    private static final FakeHome fakeHome = new FakeHome();

    private static final Wallet wallet = Unchecked.supplier(fakeHome::createEagerWallet).get();

    private static FrontPage frontPage;

    @BeforeClass
    public static void start() {
        frontPage = new FrontPage(
                ImmutableMap.of(
                        "protocol", Triada.TEST_NETWORK,
                        "version", Triada.VERSION
                ),
                new SingleThreadScoreFarm(
                        Unchecked.supplier(folder::newFolder).get(),
                        3,
                        "NOPREFIX@ffffffffffffffff"
                ),
                Unchecked.supplier(() -> folder.newFile("ledger.csv")).get(),
                new Wallets(wallet.file().getParentFile()),
                new RemoteNodes(Unchecked.supplier(() -> folder.newFolder("remotes")).get()),
                8080
        );
        final VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(1000 * 60 * 60);
        final Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(frontPage);
    }

    @Test
    public void testProtocolAndVersion() {
        final RestTemplate template = new RestTemplate();
        final ResponseEntity<String> protocol = template.getForEntity("http://localhost:8080/protocol", String.class);
        final ResponseEntity<String> version = template.getForEntity("http://localhost:8080/version", String.class);

        assertEquals(Triada.TEST_NETWORK, protocol.getBody());
        assertEquals(Triada.VERSION, version.getBody());
    }

    @AfterClass
    public static void close() throws Exception {
        frontPage.close();
    }
}
