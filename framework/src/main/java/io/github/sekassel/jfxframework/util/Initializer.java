package io.github.sekassel.jfxframework.util;

/**
 * Functional interface to initialize a node with an item.
 *
 * @param <T> The type of the node
 * @param <E> The type of the item
 */
@FunctionalInterface
public interface Initializer<T, E> {

    /**
     * Initializes the node with the given item.
     *
     * @param node The node to initialize
     * @param item The item to initialize the node with
     */
    void initialize(T node, E item);

}
