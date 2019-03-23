package io.triada.commands;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.JsonObject;
import io.triada.commands.remote.ElectCommand;
import io.triada.commands.remote.RemoteNodes;
import io.triada.models.score.Score;
import io.triada.models.score.SuffixScore;
import io.triada.node.farm.Farm;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.http.MediaType;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public final class TestElectCommand extends Assert {

    @Rule
    public final WireMockRule fileService = new WireMockRule(options().port(SuffixScore.ZERO.address().getPort()), false);

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void initWireMock() {
        this.fileService.stubFor(
                get(urlEqualTo("/"))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(
                                okJson(resJO())
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                        .withStatus(200)
                        )
        );
    }

    @Test
    public void testElectARemote() throws Exception {
        final RemoteNodes nodes = new RemoteNodes(this.temporaryFolder.newFile());
        nodes.add(SuffixScore.ZERO.address());
        final List<Score> winners = new ElectCommand(nodes, new Farm.Empty()).run(new String[]{"-r_elect", "-ignore_score_weakness"});
        assertTrue(winners.size()==1);

    }

    private static String resJO(){
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("score", SuffixScore.ZERO.asJson());
        return jsonObject.toString();
    }
}
