package io.triada.models.cli;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Execute shell script
 */
public final class ShellScript implements CommandLineInterface<String> {

    @Override
    public String executeCommand(final String command) throws IOException {
        try {
            final Process process = Runtime.getRuntime().exec(command);
            try (final InputStream stream = process.getInputStream()) {
                final String content = IOUtils.toString(stream, "UTF-8");
                process.waitFor();
                return content;
            } finally {
                process.destroy();
            }
        } catch (final InterruptedException exc) {
            Thread.currentThread().interrupt();
            throw new IOException(exc);
        }
    }
}
