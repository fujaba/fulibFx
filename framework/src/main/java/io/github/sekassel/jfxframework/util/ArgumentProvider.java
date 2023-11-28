package io.github.sekassel.jfxframework.util;

@FunctionalInterface
public interface ArgumentProvider<T, E> {

    T get(E argument);

}
