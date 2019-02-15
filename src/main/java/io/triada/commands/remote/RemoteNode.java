package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.http.HttpFileClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RemoteNode {
    private final HostAndPort hostAndPort;

    private final int score;

    private final boolean master;

    private final String network;

    public RemoteNode(
            final HostAndPort hostAndPort,
            final int score,
            final boolean master,
            final String network
    ) {
        this.hostAndPort = hostAndPort;
        this.score = score;
        this.master = master;
        this.network = network;
    }


    public boolean isMaster() {
        return this.master;
    }

    public HttpFileClient http(final String path) {
        return new HttpFileClient(
                String.format(
                        "http:://%s:%d/%s",
                        this.hostAndPort.getHost(),
                        this.hostAndPort.getPort(),
                        path
                )
        );
    }
}
