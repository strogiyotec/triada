package io.triada.functions;

@FunctionalInterface
public interface CheckedRunnable {

    void run() throws Exception;
}
