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

    /**
     * Argc
     */
    private final Map<String, String> argc;

    /**
     * Farm
     */
    private final Farm farm;

    /**
     * Ledger csv file
     */
    private final File ledger;

    /**
     * Wallets
     */
    private final Wallets wallets;

    /**
     * Remote nodes
     */
    private final Remotes remotes;

    /**
     * Front port
     */
    private final int port;

    /**
     * instance of http server to be closed in close method
     */
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

    /**
     * Was tested
     * Pid route
     *
     * @param router Router
     */
    private void pidRoute(final Router router) {
        router.route(HttpMethod.GET, "/pid")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(ManagementFactory.getRuntimeMXBean().getName().split("@")[0])
                );
    }

    /**
     * Was tested
     * Wallets route
     *
     * @param router Router
     */
    private void walletsRoute(final Router router) {
        router.route(HttpMethod.GET, "/wallets")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.wallets.all().stream().collect(Collectors.joining(",")))
                );
    }

    /**
     * Farm route
     * Was tested
     *
     * @param router Router
     */
    private void farmRoute(final Router router) {
        router.route(HttpMethod.GET, "/farm")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.farm.asText())
                );
    }

    /**
     * Score route
     * Was tested
     *
     * @param router Router
     */
    private void scoreRoute(final Router router) {
        router.route(HttpMethod.GET, "/score")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.best().get().asText())
                );
    }

    /**
     * Ledger route
     * Was tested
     *
     * @param router Router
     */
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

    /**
     * Protocol route
     * Was tested
     *
     * @param router Router
     */
    private void protocolRoute(final Router router) {
        router.route(HttpMethod.GET, "/protocol")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.argc.get("protocol"))

                );
    }

    // TODO: 4/4/19 Test
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

    /**
     * Version route
     * Was tested
     *
     * @param router Router
     */
    private void versionRoute(final Router router) {
        router.route(HttpMethod.GET, "/version")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.argc.get("version")));
    }

    /**
     * @return JsonArray of all remotes
     * @throws Exception if failed
     */
    private JsonArray allRemotes() throws Exception {
        final JsonArray remotes = new JsonArray();
        this.remotes.all().stream().map(Text::asText).forEach(remotes::add);

        return remotes;
    }

    /**
     * @return Best score from farm
     */
    private Supplier<Score> best() {
        return Unchecked.supplier(() -> this.farm.best().get(0));
    }
}
