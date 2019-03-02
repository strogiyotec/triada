package io.triada.commands;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.commands.fetch.FetchCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.mocks.FakeHome;
import io.triada.models.id.LongId;
import io.triada.models.score.TriadaScore;
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
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class TestFetchCommand extends Assert {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testFetchWallet() throws Exception {

        final WireMockServer server = new WireMockServer(TriadaScore.ZERO.address().getPort());
        final WireMockServer server2 = new WireMockServer(9875);
        try {
            final Wallet wallet = new FakeHome().createWallet(new LongId(), 0);
            configureWireMock(server, server2, wallet);
            final RemoteNodes nodes = new RemoteNodes(temporaryFolder.newFile("remotes"));
            final CopiesFromFile copies = new CopiesFromFile(this.temporaryFolder.newFolder(wallet.head().id()).toPath());
            nodes.clean();
            nodes.add(HostAndPort.fromParts("localhost", TriadaScore.ZERO.address().getPort()));
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

    private void configureWireMock(final WireMockServer server, final WireMockServer server2, final Wallet wallet) throws IOException {
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

    private JsonObject body() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("score", TriadaScore.ZERO.asJson());
        jsonObject.addProperty("size", 10_000L);
        jsonObject.addProperty("mtime", System.currentTimeMillis());
        return jsonObject;
    }
}
