package io.triada.node.front;

import io.triada.commands.remote.Remotes;
import io.triada.models.score.Score;
import io.triada.models.wallet.Wallets;
import io.triada.node.farm.Farm;
import io.triada.text.Text;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class FrontPage extends AbstractVerticle implements AutoCloseable {

    private final Map<String, String> argc;

    private final Farm farm;

    private final File ledger;

    private final Wallets wallets;

    private final Remotes remotes;

    private final int port;

    private HttpServer httpServer;

    @Override
    public void start() throws Exception {
        this.httpServer = this.vertx.createHttpServer();
        final Router router = Router.router(this.vertx);
        this.versionRoute(router);
        this.protocolRoute(router);
        this.pidRoute(router);
        this.ledgerRoute(router);
        this.remotesLedger(router);
        this.scoreRoute(router);
        this.farmRoute(router);
        this.walletsRoute(router);

        this.httpServer.requestHandler(router);
        this.httpServer.listen(this.port);
    }

    @Override
    public void close() throws Exception {
        if (this.httpServer != null) {
            this.httpServer.close();
        }
    }

    private void pidRoute(final Router router) {
        router.route(HttpMethod.GET, "/pid")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(ManagementFactory.getRuntimeMXBean().getName().split("@")[0])
                );
    }

    private void walletsRoute(final Router router) {
        router.route(HttpMethod.GET, "/wallets")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.wallets.all().stream().collect(Collectors.joining(",")))
                );
    }

    private void farmRoute(final Router router) {
        router.route(HttpMethod.GET, "/farm")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.farm.asText())
                );
    }

    private void scoreRoute(final Router router) {
        router.route(HttpMethod.GET, "/score")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.best().get().asText())
                );
    }

    private void ledgerRoute(final Router router) {
        final Supplier<String> content = Unchecked.supplier(() -> {
            if (this.ledger.exists()) {
                return FileUtils.readFileToString(this.ledger, StandardCharsets.UTF_8);
            } else {
                return "";
            }
        });
        router.route(HttpMethod.GET, "/ledger")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(content.get())
                );
    }

    private void protocolRoute(final Router router) {
        router.route(HttpMethod.GET, "/protocol")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.argc.get("protocol"))

                );
    }

    private void remotesLedger(final Router router) {
        router.route(HttpMethod.GET, "/remotes")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                        .setStatusCode(200)
                        .end(
                                new JsonObject()
                                        .put("version", this.argc.get("version"))
                                        .put("score", this.best().get().hash())
                                        .put("time", new Date().toString())
                                        .put("allRemotes", Unchecked.supplier(this::allRemotes).get())
                                        .toString()
                        )

                );
    }

    private void versionRoute(final Router router) {
        router.route(HttpMethod.GET, "/version")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.argc.get("version")));
    }

    private JsonArray allRemotes() throws Exception {
        final JsonArray remotes = new JsonArray();
        this.remotes.all().stream().map(Text::asText).forEach(remotes::add);

        return remotes;
    }

    private Supplier<Score> best() {
        return Unchecked.supplier(() -> this.farm.best().get(0));
    }
}
