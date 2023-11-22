package io.github.sekassel.jfxframework.util;

@FunctionalInterface
public interface Initializer<T, E> {

    void initialize(T node, E item);

}
