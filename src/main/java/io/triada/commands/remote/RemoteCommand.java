package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.commands.Command;
import io.triada.node.ConstNodeData;
import io.triada.node.NodeData;
import io.triada.node.farm.Farm;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Remote command
 */
@AllArgsConstructor
public final class RemoteCommand implements Command {

    private final Remotes remotes;

    private final Farm farm;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(Command.options(), argc);
        if (cmd.hasOption("-remote")) {
            final RemoteParams remoteParams = new RemoteParams(Arrays.asList(cmd.getOptionValues("remote")));
            if (remoteParams.show()) {
                this.show();
            }
            if (remoteParams.clean()) {
                this.clean();
            }
            if (remoteParams.reset()) {
                this.clean();
                this.masters(remoteParams);
            }
            if (remoteParams.masters()) {
                this.masters(remoteParams);
            }
            if (remoteParams.add()) {
                this.add(remoteParams);
            }
            if (remoteParams.remove()) {
                this.remove(remoteParams);
            }
        } else {
            throw new IllegalArgumentException("Add remote param");
        }

    }

    /**
     * Add all masters
     *
     * @param remoteParams Params
     * @throws Exception if failed
     */
    private void masters(final RemoteParams remoteParams) throws Exception {
        for (final NodeData master : ConstNodeData.MASTERS) {
            if (!remoteParams.ignore(master.host(), master.port())) {
                this.remotes.add(master.host(), master.port());
            }
        }
        System.out.println("Masters were added to the list");
    }

    private void remove(final RemoteParams params) throws Exception {
        this.remotes.remove(params.host(), params.port());
        System.out.println("Node was removed");
    }

    /**
     * Clean all nodes
     *
     * @throws Exception if failed
     */
    private void clean() throws Exception {
        int size = this.remotes.all().size();
        this.remotes.clean();
        System.out.printf(
                "All %d nodes deleted",
                size
        );
    }

    /**
     * Show all remotes
     *
     * @throws Exception if failed
     */
    private void show() throws Exception {
        final List<NodeData> all = this.remotes.all();
        for (final NodeData nodeData : all) {
            System.out.printf(
                    String.join(
                            ",",
                            "%s:%d %d",
                            " errors : %d",
                            "master ? %d\n"
                    ),
                    nodeData.host(),
                    nodeData.host(),
                    nodeData.score(),
                    nodeData.errors(),
                    nodeData.master()
            );
        }
    }

    /**
     * Add new node
     *
     * @param params Remote params
     * @throws Exception if failed
     */
    private void add(final RemoteParams params) throws Exception {
        final HostAndPort hostAndPort = HostAndPort.fromParts(params.host(), params.port());
        if (params.ignore(hostAndPort.getHost(), hostAndPort.getPort())) {
            System.out.printf("Can't add remote with address %s:%d because ignore option\n", hostAndPort.getHost(), hostAndPort.getPort());
            return;
        }
        if (!params.skipPing() && !RemoteCommand.ping(hostAndPort)) {
            System.out.printf(
                    "Can't add node [%s:%d] ,Connection timeout or wrong http status code%s",
                    hostAndPort.getHost(),
                    hostAndPort.getPort(),
                    System.lineSeparator()
            );
        }
        if (this.remotes.exists(hostAndPort)) {
            System.out.printf(
                    "Can't add node [%s:%d] Node already exists%s",
                    hostAndPort.getHost(),
                    hostAndPort.getPort(),
                    System.lineSeparator()
            );
            return;
        }
        this.remotes.add(hostAndPort);
        System.out.printf(
                "Node %s:%d was added to the list%s",
                hostAndPort.getHost(),
                hostAndPort.getPort(),
                System.lineSeparator()
        );
    }

    /**
     * @param hostAndPort to ping
     * @return True if received http status 200
     */
    private static boolean ping(final HostAndPort hostAndPort) {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);

        final RestTemplate template = new RestTemplate(factory);
        final ResponseEntity<byte[]> exchange = template.exchange(
                String.format(
                        "http://%s:%d",
                        hostAndPort.getHost(),
                        hostAndPort.getPort()
                ),
                HttpMethod.GET,
                new HttpEntity<>(Collections.emptyMap()),
                byte[].class,
                Collections.emptyMap()
        );
        return exchange.getStatusCodeValue() == 200;

    }


}
