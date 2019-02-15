package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.commands.Command;
import io.triada.node.Farm;
import io.triada.node.NodeData;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Remote command
 */
@AllArgsConstructor
public final class RemoteCommand implements Command {

    private static final Options OPTIONS = Command.remoteOptions();

    private final Remotes remotes;

    private final Farm farm;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(OPTIONS, argc);
        if (cmd.hasOption("-rclean")) {
            this.clean();
        } else if (cmd.hasOption("-radd")) {
            this.add(cmd.getOptionValues("radd"), cmd);
        } else if (cmd.hasOption("-r_elect")) {

        }
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

    private void elect() throws Exception {
        final List<NodeData> all = this.remotes.all();
        final List<NodeData> elects = new ArrayList<>(all.size());
        throw new UnsupportedOperationException("Need to implement adter iterator method of remoteNodes");
    }

    /**
     * Add new node
     *
     * @param argc Node params
     * @param cmd  {@link CommandLine}
     * @throws Exception if failed
     */
    private void add(final String[] argc, final CommandLine cmd) throws Exception {
        final HostAndPort hostAndPort = HostAndPort.fromParts(argc[0], Integer.parseInt(argc[1]));
        if (!cmd.hasOption("-skip_ping") && !RemoteCommand.ping(hostAndPort)) {
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
