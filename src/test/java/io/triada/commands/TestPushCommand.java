package io.triada.commands;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.JsonObject;
import io.triada.commands.push.PushCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.models.score.SuffixScore;
import io.triada.models.wallet.EagerWallets;
import io.triada.models.wallet.Wallet;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public final class TestPushCommand extends Assert {

    @ClassRule
    public static final WireMockRule fileService = new WireMockRule(options().port(SuffixScore.ZERO.address().getPort()), false);

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final FakeHome fakeHome = new FakeHome();

    /**
     * As we have 5 tests with 5 wallets , this array will keep them all (id of each wallet)
     */
    private static final Wallet[] wallets = new Wallet[5];

    @BeforeClass
    public static void initWireMock() throws Exception {
        testPushesWallet(fileService);
        testPushesFailsWhenOnlyEdgesNodes(fileService);
        testPushesFailsWhenOnlyOneNode(fileService);
        testPushesMultipleWallets(fileService);
    }

    @Test
    public void testPushesWallet() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        nodes.add("localhost", SuffixScore.ZERO.address().getPort());
        new PushCommand(
                new EagerWallets(
                        wallets[0].file().getParentFile()
                ),
                nodes
        ).run(new String[]{
                "-push",
                "ids=" + wallets[0].head().id(),
                "tolerate-edges",
                "tolerate-quorum=1"
        });
    }

    @Test
    public void testPushesMultipleWallets() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        nodes.add("localhost", SuffixScore.ZERO.address().getPort());
        new PushCommand(
                new EagerWallets(
                        wallets[1].file().getParentFile()
                ),
                nodes
        ).run(new String[]{
                "-push",
                "ids=" + wallets[1].head().id() + "," + wallets[2].head().id(),
                "tolerate-edges",
                "tolerate-quorum=1"
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testPushesFailsWhenOnlyEdgesNodes() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        nodes.add("localhost", SuffixScore.ZERO.address().getPort());
        new PushCommand(
                new EagerWallets(
                        wallets[3].file().getParentFile()
                ),
                nodes
        ).run(new String[]{
                "-push",
                "ids=" + wallets[3].head().id(),
        });
    }

    @Test(expected = IllegalStateException.class)
    public void testPushesFailsWhenOnlyOneNode() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        nodes.add("localhost", SuffixScore.ZERO.address().getPort());
        new PushCommand(
                new EagerWallets(
                        wallets[4].file().getParentFile()
                ),
                nodes
        ).run(new String[]{
                "-push",
                "ids=" + wallets[4].head().id(),
                "tolerate-edges"
        });
    }

    private static void testPushesWallet(final WireMockRule wireMock) throws Exception {
        final Wallet wallet = fakeHome.createEagerWallet();
        wireMock.stubFor(
                put(urlEqualTo("/wallet/" + wallet.head().id()))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(aResponse().withBody("{}").withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE).withStatus(304))
        );
        wallets[0] = wallet;
    }

    private static void testPushesMultipleWallets(final WireMockRule wireMock) throws Exception {
        final Wallet wallet = fakeHome.createEagerWallet();
        final Wallet wallet2 = fakeHome.createEagerWallet(wallet);
        wireMock.stubFor(
                put(urlEqualTo("/wallet/" + wallet.head().id()))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(
                                badRequestEntity()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                        .withStatus(304)
                                        .withBody(new JsonObject().toString())
                        )
        );
        wireMock.stubFor(
                put(urlEqualTo("/wallet/" + wallet2.head().id()))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(
                                badRequestEntity()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                        .withStatus(304)
                                        .withBody(new JsonObject().toString())
                        )
        );
        wallets[1] = wallet;
        wallets[2] = wallet2;
    }

    private static void testPushesFailsWhenOnlyEdgesNodes(final WireMockRule wireMock) throws Exception {
        final Wallet wallet = fakeHome.createEagerWallet();
        wireMock.stubFor(
                put(urlEqualTo("/wallet/" + wallet.head().id()))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(
                                badRequestEntity()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                        .withStatus(304)
                                        .withBody(new JsonObject().toString())
                        )
        );
        wallets[3] = wallet;
    }

    private static void testPushesFailsWhenOnlyOneNode(final WireMockRule wireMock) throws Exception {
        final Wallet wallet = fakeHome.createEagerWallet();
        wireMock.stubFor(
                put(urlEqualTo("/wallet/" + wallet.head().id()))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(
                                badRequestEntity()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                        .withStatus(304)
                                        .withBody(new JsonObject().toString())
                        )
        );
        wallets[4] = wallet;
    }
}
