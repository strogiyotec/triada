package io.triada.node;

import com.google.common.collect.ImmutableMap;
import io.triada.Triada;
import io.triada.node.front.FrontPage;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public final class TestFront extends Assert {
    private static final FrontPage frontPage = new FrontPage(ImmutableMap.of("protocol", Triada.TEST_NETWORK, "version", Triada.VERSION), Collections.emptyMap(), 8080);

    @BeforeClass
    public static void start() {
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
