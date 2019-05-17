package io.triada.node.front;

import io.triada.commands.remote.Remotes;
import io.triada.models.id.LongId;
import io.triada.models.score.Score;
import io.triada.models.tax.TxnTaxes;
import io.triada.models.wallet.Wallet;
import io.triada.models.wallet.Wallets;
import io.triada.node.entrance.Entrance;
import io.triada.node.farm.Farm;
import io.triada.text.Text;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
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
     * Boolean flags
     */
    private final Map<String, Boolean> flags;

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
     * Entrance
     */
    private final Entrance entrance;

    /**
     * instance of http server to be closed in close method
     */
    private HttpServer httpServer;

    @Override
    public void start() throws Exception {
        this.httpServer = this.vertx.createHttpServer();
        final Router router = Router.router(this.vertx);
        router.route().handler(BodyHandler.create());
        this.versionRoute(router);
        this.protocolRoute(router);
        this.getWalletRoute(router);
        this.pidRoute(router);
        this.ledgerRoute(router);
        this.remotesRoute(router);
        this.scoreRoute(router);
        this.farmRoute(router);
        this.walletsRoute(router);
        this.putWalletRoute(router);

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

    /**
     * Remotes route
     * Was tested
     *
     * @param router Router
     */
    private void remotesRoute(final Router router) {
        router.route(HttpMethod.GET, "/remotes")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                        .setStatusCode(200)
                        .end(
                                new JsonObject()
                                        .put("version", this.argc.get("version"))
                                        .put("score", this.best().get().hash())
                                        .put("time", new Date().toString())
                                        .put("remotes", Unchecked.supplier(this::allRemotes).get())
                                        .toString()
                        )

                );
    }

    /**
     * Get json representation of wallet
     *
     * @param router Router
     */
    private void getWalletRoute(final Router router) {
        router.get("/wallet/:id")
                .handler(event -> {
                    final String walletId = event.request().getParam("id");
                    final Wallet wallet = Unchecked.supplier(() -> this.wallets.acq(walletId)).get();
                    final TxnTaxes taxes = new TxnTaxes(wallet);
                    final JsonObject body = new JsonObject()
                            .put("version", this.argc.get("version"))
                            .put("protocol", this.argc.get("protocol"))
                            .put("id", wallet.asText())
                            .put("score", new JsonObject(this.best().get().asJson().toString()))
                            .put("taxes", taxes.paid())
                            .put("debt", taxes.debt());

                    event.request().response()
                            .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                            .setStatusCode(200)
                            .end(body.toString());
                });
    }

    /**
     * Put wallet
     *
     * @param router Router
     *               // TODO: 5/16/19 Add test
     */
    private void putWalletRoute(final Router router) {
        router.put("/wallet/:id")
                .handler(event -> {
                    if (this.flags.getOrDefault("disable-push", true)) {
                        event.request().response()
                                .setStatusCode(404)
                                .end();
                        return;
                    }

                    final List<String> modified =
                            Unchecked.supplier(
                                    () ->
                                            this.entrance.push(
                                                    new LongId(event.request().getParam("id")).asText(),
                                                    event.getBodyAsString()
                                            )
                            ).get();

                    if (modified.isEmpty()) {
                        event.request().response().setStatusCode(304).end();
                        return;
                    }

                    event.request().response()
                            .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                            .setStatusCode(200)
                            .end(
                                    new JsonObject()
                                            .put("version", this.argc.get("version"))
                                            .put("score", new JsonObject(this.best().get().asJson().toString()))
                                            .put("wallets", this.wallets.count())
                                            .toString()
                            );
                }).failureHandler(event -> event.failure().printStackTrace());

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
