package io.github.sekassel.jfxframework.util;

/**
 * Functional interface to provide a value from an argument.
 *
 * @param <T> The type of the value to provide
 * @param <E> The type of the argument
 */
@FunctionalInterface
public interface ArgumentProvider<T, E> {

    /**
     * Returns the value for the given argument.
     *
     * @param argument The argument to get the value for
     * @return The value
     */
    T get(E argument);

}
