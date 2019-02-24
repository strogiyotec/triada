package io.triada.commands;

public interface ValuableCommand<T> {

    T run(String argc[]) throws Exception;
}
