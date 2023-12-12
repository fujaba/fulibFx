package io.github.sekassel.jfxframework.util;

/**
 * Functional interface to initialize an object with an item.
 *
 * @param <T> The type of the object
 * @param <E> The type of the item
 */
@FunctionalInterface
public interface Initializer<T, E> {

    /**
     * Initializes the object with the given item.
     *
     * @param object The object to initialize
     * @param item   The item to initialize the object with
     */
    void initialize(T object, E item);

}
