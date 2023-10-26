package io.github.sekassel.jfxframework.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A tree that can be traversed by a path in form of directory-like structure.
 *
 * @param <E> The type of the values stored in the tree.
 */
public interface TraversableTree<E> {

    /**
     * Returns the value of the root node.
     *
     * @return The value of the root node.
     */
    @Nullable E root();

    /**
     * Traverses the tree to the given path. The current node will be set to the node at the given path.
     *
     * @param path The path to traverse to.
     * @return The value at the given path, or null if the path does not exist.
     */
    @Nullable E traverse(String path);

    /**
     * Returns the value of the tree at the given path.
     *
     * @param path The path to traverse to.
     * @return The value at the given path, or null if the path does not exist.
     */
    @Nullable E get(String path);

    /**
     * Checks if there is a node at the given path.
     */
    boolean containsPath(String path);

    /**
     * Returns the value of the currently visited node.
     *
     * @return The value of the current node.
     */
    @Nullable E current();

    /**
     * Inserts a value at the given path. If the path does not exist, it will be created.
     * <p>
     * If the path already exists, the value will be overwritten.
     *
     * @param path  The path to insert the value at.
     * @param value The value to insert.
     */
    void insert(@NotNull String path, @NotNull E value);

}
