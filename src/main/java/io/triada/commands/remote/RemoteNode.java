package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.http.HttpTriadaClient;
import io.triada.models.score.Score;
import io.triada.models.score.TriadaScore;
import io.triada.node.NodeData;
import io.triada.node.farm.Farm;
import io.triada.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class RemoteNode implements Text {
    /**
     * HostAndPorn of Remote node
     */
    private final HostAndPort hostAndPort;

    /**
     * Score of node
     */
    private final Score score;

    /**
     * Is score is master
     */
    private final boolean master;

    /**
     * Network name
     */
    private final String network;

    /**
     * Id of node
     */
    private final int idx;

    /**
     * Is Http client from node was touched , become true when http is called
     */
    private boolean touched = false;

    public RemoteNode(
            final HostAndPort hostAndPort,
            final Score score,
            final boolean master,
            final String network,
            final int idx
    ) {
        this.hostAndPort = hostAndPort;
        this.score = score;
        this.master = master;
        this.network = network;
        this.idx = idx;
    }

    public RemoteNode(final Farm farm, final NodeData node, final String network, final int idx) throws Exception {
        final List<Score> best = farm.best();
        this.hostAndPort = HostAndPort.fromParts(node.host(), node.port());
        this.score = best.isEmpty() ? TriadaScore.ZERO : best.get(0);
        this.master = node.master();
        this.network = network;
        this.idx = idx;
    }

    public HostAndPort address() {
        return hostAndPort;
    }

    public boolean touched() {
        return this.touched;
    }

    public boolean isMaster() {
        return this.master;
    }

    /**
     * @param path Path
     * @return HttpClient with this path
     */
    public HttpTriadaClient http(final String path) {
        this.touched = true;
        return new HttpTriadaClient(
                String.format(
                        "http://%s:%d/%s",
                        this.hostAndPort.getHost(),
                        this.hostAndPort.getPort(),
                        path
                )
        );
    }

    @Override
    public String asText() {
        return String.format(
                "%s:%d/%d\n",
                this.hostAndPort.getHost(),
                this.hostAndPort.getPort(),
                this.idx
        );
    }
}
