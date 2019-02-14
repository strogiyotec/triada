package io.triada.commands.remote;

import com.google.common.net.HostAndPort;
import io.triada.commands.Command;
import io.triada.node.farm.Farm;
import lombok.AllArgsConstructor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Remote command
 */
@AllArgsConstructor
public final class RemoteCommand implements Command {

    private static final Options OPTIONS = RemoteCommand.remoteOptions();

    private final Remotes remotes;

    private final Farm farm;

    @Override
    public void run(final String[] argc) throws Exception {
        final CommandLine cmd = new DefaultParser().parse(OPTIONS, argc);
        if (cmd.hasOption("-rclean")) {
            this.clean();
        } else if (cmd.hasOption("-radd")) {
            this.add(cmd.getOptionValues("radd"), cmd);
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
                    "Can't add node [%s:%d] ,Connection timeout or wrong http status code",
                    hostAndPort.getHost(),
                    hostAndPort.getPort()
            );
        }
        if (this.remotes.exists(hostAndPort)) {
            System.out.printf(
                    "Can't add node [%s:%d] Node already exists",
                    hostAndPort.getHost(),
                    hostAndPort.getPort()
            );
        }
        this.remotes.add(hostAndPort);
        System.out.printf(
                "Node %s:%d was added to the list",
                hostAndPort.getHost(),
                hostAndPort.getPort()
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

    /**
     * @return Predefined list of options
     */
    private static Options remoteOptions() {
        return new Options()
                .addOption(
                        new Option("rclean", false, "Remove all registered nodes")
                ).addOption(
                        new Option("show", false, "Show all registered remote nodes")
                ).addOption(
                        new Option("reset", false, "Restore it back to the default list of nodes")
                ).addOption(
                        new Option("masters", false, "Add all \"master\" nodes to the list")
                ).addOption(
                        RemoteCommand.remoteAdd()
                ).addOption(
                        Option.builder()
                                .numberOfArgs(3)
                                .argName("remove")
                                .longOpt("remove")
                                .argName("host")
                                .longOpt("host")
                                .hasArgs()
                                .argName("port")
                                .longOpt("port")
                                .hasArgs()
                                .desc(" Remove the remote node")
                                .build()
                ).addOption(
                        new Option("elect", false, "Pick a random remote node as a target for a bonus awarding")
                ).addOption(
                        new Option("trim", false, "Remove the least reliable nodes")
                ).addOption(
                        Option.builder()
                                .numberOfArgs(2)
                                .argName("select")
                                .longOpt("select")
                                .argName("amount")
                                .longOpt("amount")
                                .hasArgs()
                                .desc("Select the strongest n nodes.")
                                .build()
                ).addOption(
                        new Option("skip_ping", false, "Skip ping")
                );
    }

    /**
     * @return Remote add option
     */
    private static Option remoteAdd() {
        final Option option = new Option("radd", true, "Add new Remote");
        option.setArgs(2);
        option.setValueSeparator(' ');

        return option;
    }
}
