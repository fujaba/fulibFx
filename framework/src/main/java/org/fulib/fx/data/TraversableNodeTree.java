package org.fulib.fx.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import static org.fulib.fx.util.FrameworkUtil.error;

/**
 * A tree that can be traversed by a path in form of directory-like structure implemented using nodes.
 *
 * @param <E> The type of the values stored in the tree.
 */
public class TraversableNodeTree<E> implements TraversableTree<E> {

    private final @NotNull Node<E> root;

    private @NotNull Node<E> current;

    /**
     * Creates a new traversable tree.
     */
    public TraversableNodeTree() {
        this.root = new Node<>("", null, null, null);
        this.current = this.root;
    }

    @Override
    public @Nullable E root() {
        return this.root.value();
    }

    @Override
    public E traverse(String path) {
        return this.follow(path, true);
    }

    @Override
    public @Nullable E get(String path) {
        return this.follow(path, false);
    }

    @Override
    public boolean containsPath(String path) {
        return this.follow(path, false) != null;
    }

    @Override
    public @Nullable E current() {
        return this.current.value();
    }

    /**
     * Inserts a value at the given path. If the path does not exist, it will be created.
     * <p>
     * If the path starts with a slash, the root node will be used as the starting point.
     * <p>
     * If the path does not start with a slash, the current node will be used as the starting point.
     *
     * @param path  The path to insert the value at.
     * @param value The value to insert.
     */
    @Override
    public void insert(@NotNull String path, @NotNull E value) {

        Node<E> node = path.startsWith("/") ? this.root : this.current;

        for (String element : path.split("/")) {
            if (element.isBlank()) {
                continue;
            }

            if (element.equals("..")) {
                if (node.parent == null) {
                    throw new IllegalArgumentException(error(3006));
                }
                node = node.parent;
                continue;
            }

            Node<E> traversed = node;
            node = node.children().parallelStream().filter(child -> child.id().equals(element)).findAny().orElseGet(() -> {
                Node<E> newChild = new Node<>(element, null, null, null);
                traversed.addChild(newChild);
                return newChild;
            });

        }
        node.value(value);
    }

    public Node<E> currentNode() {
        return this.current;
    }

    public void setCurrentNode(Node<E> node) {
        this.current = node;
    }

    /**
     * Follows the given path and returns the value at the end of the path.
     * <p>
     * If the path starts with a slash, the root node will be used as the starting point.
     * <p>
     * If the path does not start with a slash, the current node will be used as the starting point.
     *
     * @param path     The path to follow.
     * @param navigate Whether the current node should be changed to the node at the end of the path.
     * @return The value at the end of the path, or null if the path does not exist.
     */
    private E follow(String path, boolean navigate) {
        Node<E> node = path.startsWith("/") ? this.root : this.current;

        for (String element : path.split("/")) {
            if (element.isBlank()) {
                continue;
            }

            if (node == null) {
                return null;
            }

            if (element.equals("..")) {
                if (node.parent != null) {
                    node = node.parent;
                }
                continue;
            }

            node = node.children().parallelStream().filter(child -> child.id().equals(element)).findAny().orElse(null);

        }
        if (navigate && node != null) {
            this.current = node;
        }
        return node == null ? null : node.value();
    }

    /**
     * A node for representing a tree.
     *
     * @param <E> The type of the value stored in the node (and the tree).
     */
    public static class Node<E> {

        private final @NotNull String id;
        private @Nullable E value;
        private @Nullable Node<E> parent;
        private @Nullable Collection<Node<E>> children;

        public Node(@NotNull String id, @Nullable E value, @Nullable Node<E> parent, @Nullable Collection<Node<E>> children) {
            this.id = id;
            this.value = value;
            this.parent = parent;
            this.children = children;
        }

        public @NotNull String id() {
            return this.id;
        }

        public @Nullable E value() {
            return this.value;
        }

        public @Nullable Node<E> parent() {
            return this.parent;
        }

        public @NotNull Collection<Node<E>> children() {
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            return this.children;
        }

        public void value(@Nullable E value) {
            this.value = value;
        }

        public void addChild(Node<E> child) {
            this.children().add(child);
            if (child.parent != null) {
                child.parent.removeChild(child);
            }
            child.parent = this;
        }

        public void removeChild(Node<E> child) {
            child.parent = null;
            if (this.children == null) {
                return;
            }
            this.children.remove(child);
        }

    }

}
