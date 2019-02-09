package io.triada.http;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import static org.hamcrest.Matchers.containsString;

public final class TestHttp extends Assert {

    /**
     * Test port
     */
    private static final int TEST_PORT = 8081;

    /**
     * Test path
     */
    private static final String URL = "/file";


    /**
     * Test service that return file
     */
    @Rule
    public WireMockRule fileService = new WireMockRule(options().port(TEST_PORT), false);

    @Before
    public void setUp() throws Exception {
        fileService.stubFor(
                get(urlEqualTo(URL))
                        .withHeader("Accept", equalTo(APPLICATION_OCTET_STREAM.toString()))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", APPLICATION_OCTET_STREAM.toString())
                                        .withStatus(200)
                                        .withBodyFile("test.txt")
                        )

        );
    }

    @Test
    public void testDownload() throws Exception {
        final HttpClient httpClient = new HttpClient(String.format(
                "http://localhost:%d%s",
                TEST_PORT,
                URL
        ));
        final File file = ResourceUtils.getFile(this.getClass().getResource("/__files/from_http.txt"));

        assertThat(FileUtils.readFileToString(httpClient.getFile(file), StandardCharsets.UTF_8), containsString("Hello my friend"));

    }
}
