package io.triada.commands;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.commands.remote.RemoteNodes;
import io.triada.dates.DateConverters;
import io.triada.mocks.FakeHome;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.transaction.ValidatedTxn;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public final class TestTaxCommand extends Assert {

    @Rule
    public WireMockRule fileService = new WireMockRule(options().port(80), false);

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    public void testPayTaxes() throws Exception {
        Wallet wallet = new FakeHome().createWallet(new LongId(), 0);
        final Wallets wallets = new Wallets(wallet.file().getParentFile());
        final TxnAmount fund = new TxnAmount(new BigDecimal("19.99"));
        final RsaKey key = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        for (int i = 0; i < 10; i++) {
            wallet = wallet.add(
                    new SignedTriadaTxn(
                            new ValidatedTxn(
                                    String.valueOf(i + 1),
                                    DateConverters.nowMinusYears(300),
                                    fund,
                                    "NOPREFIX",
                                    new LongId(),
                                    "-"
                            ),
                            key,
                            new LongId(wallet.head().id())
                    )
            );
        }
        Score score = new TriadaScore(HostAndPort.fromParts("localhost", 80), "NOPREFIX@0000000000000000", 1);
        final RemoteNodes nodes = new RemoteNodes(this.folder.newFile("remotes"));
        nodes.add(score.address());
        for (int i = 0; i < 10; i++) {
            score = score.next();
        }
        this.fileService.stubFor(
                put(urlEqualTo("/"))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(
                                okJson(body(score))
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                        .withStatus(200)
                        )
        );
    }


    private static String body(final Score score) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("score", score.asJson());
        return jsonObject.toString();
    }
}
