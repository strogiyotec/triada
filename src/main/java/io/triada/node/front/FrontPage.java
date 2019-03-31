package io.triada.node.front;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import lombok.RequiredArgsConstructor;

import java.lang.management.ManagementFactory;
import java.util.Map;

@RequiredArgsConstructor
public final class FrontPage extends AbstractVerticle implements AutoCloseable {

    private final Map<String, String> argc;

    private final Map<String, Object> settings;

    private final int port;

    private HttpServer httpServer;

    @Override
    public void start() throws Exception {
        this.httpServer = this.vertx.createHttpServer();
        final Router router = Router.router(this.vertx);
        this.versionRoute(router);
        this.protocolRoute(router);
        this.pidRoute(router);

        this.httpServer.requestHandler(router);
        this.httpServer.listen(this.port);
    }

    private void pidRoute(final Router router) {
        router.route(HttpMethod.GET, "/pid")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(ManagementFactory.getRuntimeMXBean().getName().split("@")[0])
                );
    }

    private void jsonRoute(final Router router) {
        router.route(HttpMethod.GET, "/")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                        .setStatusCode(200)
                        .end(new JsonObject().toString())
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

    private void versionRoute(final Router router) {
        router.route(HttpMethod.GET, "/version")
                .handler(routingContext -> routingContext.request().response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "text/plain")
                        .setStatusCode(200)
                        .end(this.argc.get("version")));
    }

    @Override
    public void close() throws Exception {
        if (this.httpServer != null) {
            this.httpServer.close();
        }
    }
}
