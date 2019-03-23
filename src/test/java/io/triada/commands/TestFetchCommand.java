package io.triada.commands;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.commands.fetch.FetchCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.models.id.LongId;
import io.triada.models.score.SuffixScore;
import io.triada.models.wallet.CopiesFromFile;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.WalletCopy;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class TestFetchCommand extends Assert {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testFetchWallet() throws Exception {

        final WireMockServer server = new WireMockServer(SuffixScore.ZERO.address().getPort());
        final WireMockServer server2 = new WireMockServer(9875);
        try {
            final Wallet wallet = new FakeHome().createWallet(new LongId(), 0);
            configureOneFailureWireMock(server, server2, wallet);
            final RemoteNodes nodes = new RemoteNodes(temporaryFolder.newFile("remotes"));
            final CopiesFromFile copies = new CopiesFromFile(this.temporaryFolder.newFolder(wallet.head().id()).toPath());
            nodes.clean();
            nodes.add(HostAndPort.fromParts("localhost", SuffixScore.ZERO.address().getPort()));
            nodes.add(HostAndPort.fromParts("localhost", 9875));
            new FetchCommand(
                    new Wallets(wallet.file().getParentFile()),
                    copies.root().toPath(),
                    nodes
            ).run(new String[]{"-fetch", "tolerate-edges", "tolerate-quorum=1", "ignore-score-weakness", "wallet=" + wallet.head().id()});
            final List<WalletCopy> all = copies.all();
            assertEquals(1, all.size());
            assertEquals("1", all.get(0).name());
            assertEquals(0, all.get(0).score());
        } finally {
            server.stop();
            server2.stop();
        }
    }

    @Test
    public void testFetchMultipleWallets() throws Exception {
        final WireMockServer server = new WireMockServer(SuffixScore.ZERO.address().getPort());
        try {
            final List<Wallet> wallets = Arrays.asList(new FakeHome().createWallet(new LongId(), 0), new FakeHome().createWallet(new LongId(), 0));
            configureWiremockForListOfWallets(server, wallets);
            final RemoteNodes remotes = new RemoteNodes(temporaryFolder.newFile("remotes2"));
            remotes.add(HostAndPort.fromParts("localhost", SuffixScore.ZERO.address().getPort()));
            final List<CopiesFromFile> copiesFromFiles =
                    Arrays.asList(
                            new CopiesFromFile(this.temporaryFolder.newFolder(wallets.get(0).head().id()).toPath()),
                            new CopiesFromFile(this.temporaryFolder.newFolder(wallets.get(1).head().id()).toPath())
                    );
            new FetchCommand(
                    new Wallets(wallets.get(0).file().getParentFile()),
                    copiesFromFiles.get(0).root().toPath(),
                    remotes
            ).run(new String[]{"-fetch", "tolerate-edges", "tolerate-quorum=1", "ignore-score-weakness", "wallet=" + wallets.get(0).head().id() + "," + wallets.get(1).head().id()});
            for (final CopiesFromFile copiesFromFile : copiesFromFiles) {
                final List<WalletCopy> all = copiesFromFile.all();
                assertEquals(1, all.size());
                assertEquals("1", all.get(0).name());
                assertEquals(0, all.get(0).score());
            }

        } finally {
            server.stop();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testFailsWhenOnlyEdgesNodes() throws Exception {
        final WireMockServer server = new WireMockServer(SuffixScore.ZERO.address().getPort());
        try {
            final Wallet wallet = new FakeHome().createWallet(new LongId(), 0);
            configureWiremockForListOfWallets(server, Collections.singletonList(wallet));
            final RemoteNodes remotes = new RemoteNodes(temporaryFolder.newFile("remotes3"));
            remotes.add(HostAndPort.fromParts("localhost", SuffixScore.ZERO.address().getPort()));
            final CopiesFromFile copy = new CopiesFromFile(this.temporaryFolder.newFolder(wallet.head().id()).toPath());
            new FetchCommand(
                    new Wallets(wallet.file().getParentFile()),
                    copy.root().toPath(),
                    remotes
            ).run(new String[]{"-fetch", "ignore-score-weakness", "wallet=" + wallet.head().id()});
        } finally {
            server.stop();
        }
    }

    private void configureOneFailureWireMock(final WireMockServer server, final WireMockServer server2, final Wallet wallet) throws IOException {
        server.stubFor(
                get(urlEqualTo("/wallet/" + wallet.head().id()))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                        .withStatus(200)
                                        .withBody(body().toString())
                        )

        );
        server.stubFor(get(urlEqualTo("/wallet/" + wallet.head().id() + ".bin"))
                .withHeader("Accept", equalTo(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                .withStatus(200)
                                .withBody(Files.readAllBytes(wallet.file().toPath()))
                )

        );
        server2.stubFor(get(urlEqualTo("/wallet/" + wallet.head().id()))
                .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .willReturn(aResponse().withStatus(404)));
        server.start();
        server2.start();
    }

    private void configureWiremockForListOfWallets(final WireMockServer server, final List<Wallet> wallets) throws Exception {
        for (final Wallet wallet : wallets) {
            server.stubFor(
                    get(urlEqualTo("/wallet/" + wallet.head().id()))
                            .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                            .willReturn(
                                    aResponse()
                                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                            .withStatus(200)
                                            .withBody(body().toString())
                            )

            );
            server.stubFor(get(urlEqualTo("/wallet/" + wallet.head().id() + ".bin"))
                    .withHeader("Accept", equalTo(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .willReturn(
                            aResponse()
                                    .withHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                    .withStatus(200)
                                    .withBody(Files.readAllBytes(wallet.file().toPath()))
                    )

            );
            server.start();

        }

    }

    private JsonObject body() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("score", SuffixScore.ZERO.asJson());
        jsonObject.addProperty("size", 10_000L);
        jsonObject.addProperty("mtime", System.currentTimeMillis());
        return jsonObject;
    }
}
