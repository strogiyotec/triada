package io.triada.commands;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import io.triada.commands.remote.RemoteNodes;
import io.triada.commands.taxes.TaxesCommand;
import io.triada.dates.DateConverters;
import io.triada.mocks.FakeHome;
import io.triada.models.amount.Amount;
import io.triada.models.amount.TxnAmount;
import io.triada.models.id.LongId;
import io.triada.models.key.RsaKey;
import io.triada.models.score.Score;
import io.triada.models.score.SuffixScore;
import io.triada.models.tax.TxnTaxes;
import io.triada.models.transaction.SignedTriadaTxn;
import io.triada.models.transaction.ValidatedTxn;
import io.triada.models.wallet.TriadaWallet;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public final class TestTaxCommand extends Assert {

    @Rule
    public WireMockRule fileService = new WireMockRule(options().port(9098), false);

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();


    @Test
    public void testPayTaxes() throws Exception {

        final Wallet wallet = new FakeHome().createEagerWallet(new LongId());
        final Wallets wallets = new Wallets(wallet.file().getParentFile());
        final TxnAmount fund = new TxnAmount(new BigDecimal("19.99"));
        final RsaKey key = new RsaKey(ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")));
        for (int i = 0; i < 10; i++) {
            wallet.add(
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
        Score score = new SuffixScore(HostAndPort.fromParts("localhost", 9098), "NOPREFIX@0000000000000000", 1);
        for (int i = 0; i < 10; i++) {
            score = score.next();
        }
        final RemoteNodes nodes = new RemoteNodes(this.folder.newFile("remotes"));
        nodes.add(score.address());

        this.fileService.stubFor(
                get(urlEqualTo("/"))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(
                                okJson(body(score))
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                        .withStatus(200)
                        )
        );

        final Amount<Long> before = wallet.balance();
        final TxnTaxes tax = new TxnTaxes(wallet, true);
        final long debt = tax.debt();
        new TaxesCommand(wallets, nodes)
                .run(
                        new String[]{
                                "-taxes",
                                "ignore-score-weakness",
                                "private-key=" + ResourceUtils.getFile(this.getClass().getResource("/keys/pkcs8")).getAbsolutePath(),
                                "pay",
                                "wallet=" + wallet.head().id()
                        }
                );
        final TriadaWallet after = new TriadaWallet(wallet.file());
        final TxnTaxes taxesAfter = new TxnTaxes(after);
        assertTrue(taxesAfter.paid() > 0);
        assertEquals(before.substract(debt).asText(6), after.balance().asText(6));
        this.fileService.stop();
    }


    private static String body(final Score score) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("score", score.asJson());
        return jsonObject.toString();
    }
}