package io.triada.commands;

/**
 * All cli which return a result need to implement this interface
 *
 * @param <T> Type
 */
public interface ValuableCommand<T> {

    T run(String argc[]) throws Exception;
}
