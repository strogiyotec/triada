package io.triada.functions;

@FunctionalInterface
public interface CheckedToBooleanFunction<T> {

    boolean apply(T t) throws Exception;
}
