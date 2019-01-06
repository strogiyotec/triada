package io.triada.models.cli;

import java.io.IOException;

/**
 * Execute given command
 *
 * @param <T> return type
 */
public interface CommandLineInterface<T> {

    T executeCommand(String command) throws IOException;

}
