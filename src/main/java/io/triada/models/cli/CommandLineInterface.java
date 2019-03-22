package io.triada.models.cli;

import java.io.IOException;

/**
 * Execute given command
 *
 * @param <T> return type
 */
public interface CommandLineInterface<T> {

    /**
     * @param command Command to execute
     * @return Result of given command
     * @throws IOException if failed
     */
    T executeCommand(String command) throws IOException;

}
