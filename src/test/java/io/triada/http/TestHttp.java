package io.triada.http;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.ResourceAccessException;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public final class TestHttp extends Assert {

    /**
     * Test port
     */
    private static final int TEST_PORT = 8081;

    /**
     * Test path to get file
     */
    private static final String URL_GET = "/file";

    /**
     * Test path to send file
     */
    private static final String URL_PUT = "/save";


    /**
     * Test service that return file
     */
    @Rule
    public WireMockRule fileService = new WireMockRule(options().port(TEST_PORT), false);

    @Before
    public void setUp() throws Exception {
        this.fileService.stubFor(
                get(urlEqualTo(URL_GET))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                        .withStatus(200)
                                        .withBodyFile("test.txt")
                        )

        );

        this.fileService.stubFor(
                put(urlEqualTo(URL_PUT))
                        .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
                        .willReturn(
                                okJson("{\"name\" :\"Almas\"}")
                                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                                        .withStatus(200)
                        )
        );
    }

    @Test
    public void testDownload() throws Exception {
        final HttpFileClient httpClient = new HttpFileClient(String.format(
                "http://localhost:%d%s",
                TEST_PORT,
                URL_GET
        ));
        final File file = ResourceUtils.getFile(this.getClass().getResource("/__files/from_http.txt"));

        assertThat(FileUtils.readFileToString(httpClient.getFile(file), StandardCharsets.UTF_8), containsString("Hello my friend"));

    }

    @Test
    public void tesUpload() throws Exception {
        final HttpFileClient httpClient = new HttpFileClient(String.format(
                "http://localhost:%d%s",
                TEST_PORT,
                URL_PUT
        ));
        final File file = ResourceUtils.getFile(this.getClass().getResource("/__files/test.txt"));

        assertThat(httpClient.putFile(file).get("name").getAsString(), is("Almas"));
    }

    @Test(expected = ResourceAccessException.class)
    public void testBrokenUrl() throws Exception {
        final HttpFileClient httpClient = new HttpFileClient(String.format(
                "http://localhost:4444%s",
                URL_GET
        ));
        final File file = ResourceUtils.getFile(this.getClass().getResource("/__files/from_http.txt"));

        httpClient.getFile(file);
    }
}
