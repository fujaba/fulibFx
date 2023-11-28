package io.github.sekassel.jfxframework.constructs;

import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.duplicate.Duplicators;
import io.github.sekassel.jfxframework.util.ArgumentProvider;
import io.github.sekassel.jfxframework.util.Initializer;
import io.github.sekassel.jfxframework.util.Util;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * A For loop for use in Code. Creates a node for each item in a list and adds them to the children of a container.
 * This allows you to easily display a list of items in a container like a friend list or a list of products.
 *
 * @param <E> The type of the node
 * @param <T> The type of the items in the list
 */
public class For<E, T> extends Parent {

    // List of items to iterate over
    private final SimpleObjectProperty<ObservableList<T>> list = new SimpleObjectProperty<>();
    // The nodes that are currently displayed
    private final HashMap<Object, Node> nodes = new HashMap<>();

    // The node to display for each iteration
    private Object node;

    // The container to add the nodes to
    private Parent container;

    // The provider for the node
    private ArgumentProvider<Node, T> nodeProvider;

    // The parameters to pass to the controller
    private Map<String, Object> params;

    // The children of the container (saved for performance)
    private ObservableList<Node> children;

    // Listener to the list property to update the children when the list changes
    ListChangeListener<T> listChangeListener = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                for (T item : change.getAddedSubList()) {
                    add(item);
                }
            } else if (change.wasRemoved()) {
                for (T item : change.getRemoved()) {
                    remove(item);
                }
            }
        }
    };

    // The method to call when the controller is created
    private Initializer<E, T> initializer;

    /**
     * Use the factory methods to create a new For loop.
     */
    private For() {
        list.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.removeListener(listChangeListener);
            }
            if (newValue != null) {
                newValue.addListener(listChangeListener);
            }
        });
    }

    /**
     * Creates a new For loop for use in Code and initializes it.
     * <p>
     * This factory is based on the sub controller system. It will create a new controller for each item in the list using the router.
     * <p>
     * Example: For.create(myVbox, myListOfItems, ItemController.class, Map.of("argument", value), (itemController, item) -> itemController.setItem(item));
     * <p>
     * Example: For.create(myVbox, personList, Map.of("argument", value), PersonController.class, PersonController::setPerson);
     *
     * @param container   The container to add the nodes to
     * @param list        The list of items to display
     * @param node        The node to display for each item
     * @param params      The parameters to pass to the created controller
     * @param initializer The method to call when the controller is created (useful for setting the item)
     * @param <T>         The type of the items in the list
     * @param <E>         The type of the node
     * @return The For loop
     */
    public static <T, E> For<E, T> controller(@NotNull Parent container, @NotNull ObservableList<@NotNull T> list, @NotNull Class<? extends E> node, @NotNull Map<@NotNull String, @Nullable Object> params, @NotNull Initializer<@NotNull E, @Nullable T> initializer) {
        For<E, T> forLoop = new For<>();
        forLoop.initializer = initializer;
        forLoop.setContainer(container);
        forLoop.setList(list);
        forLoop.setNode(node);
        forLoop.params(params);
        return forLoop;
    }

    /**
     * Creates a new For loop for use in Code and initializes it.
     * <p>
     * This factory is based on the sub controller system. It will create a new controller for each item in the list using the router.
     * <p>
     * Example: For.create(myVbox, myListOfItems, ItemController.class);
     *
     * @param container The container to add the nodes to
     * @param list      The list of items to display
     * @param node      The node to display for each item
     * @param <T>       The type of the items in the list
     * @param <E>       The type of the node
     * @return The For loop
     */
    public static <T, E> For<E, T> controller(@NotNull Parent container, @NotNull ObservableList<@NotNull T> list, @NotNull Class<? extends E> node) {
        return controller(container, list, node, Map.of(), (controller, item) -> {
        });
    }

    /**
     * Creates a new For loop for use in Code and initializes it.
     * <p>
     * This factory is based on the sub controller system. It will create a new controller for each item in the list using the router.
     * <p>
     * Example: For.create(myVbox, myListOfItems, ItemController.class, (itemController, item) -> itemController.setItem(item));
     * <p>
     * Example: For.create(myVbox, personList, PersonController.class, PersonController::setPerson);
     *
     * @param container   The container to add the nodes to
     * @param list        The list of items to display
     * @param node        The node to display for each item
     * @param initializer The method to call when the controller is created (useful for setting the item)
     * @param <T>         The type of the items in the list
     * @param <E>         The type of the node
     * @return The For loop
     */
    public static <T, E> For<E, T> controller(@NotNull Parent container, @NotNull ObservableList<@NotNull T> list, @NotNull Class<? extends E> node, @NotNull Initializer<@NotNull E, @Nullable T> initializer) {
        return controller(container, list, node, Map.of(), initializer);
    }

    /**
     * Creates a new For loop for use in Code and initializes it.
     * <p>
     * This factory is based on Nodes. It will duplicate the given node for each item in the list.
     * <p>
     * Example: For.create(myVbox, myListOfItems, new Label(), (label, item) -> label.setText(item.getName()));
     *
     * @param container   The container to add the nodes to
     * @param list        The list of items to display
     * @param node        The node to display for each item
     * @param initializer The method to call when the controller is created (useful for setting the item)
     * @param <T>         The type of the items in the list
     * @param <E>         The type of the node
     * @return The For loop
     */
    public static <T, E> For<E, T> node(@NotNull Parent container, @NotNull ObservableList<@NotNull T> list, @NotNull E node, @NotNull Initializer<@NotNull E, @Nullable T> initializer) {
        For<E, T> forLoop = new For<>();
        forLoop.initializer = initializer;
        forLoop.setContainer(container);
        forLoop.setList(list);
        forLoop.setNode(node);
        return forLoop;
    }

    /**
     * Creates a new For loop for use in Code and initializes it.
     * <p>
     * This factory is based on Nodes. It will duplicate the given node for each item in the list.
     * <p>
     * Example: For.create(myVbox, myListOfItems, new Label());
     *
     * @param container The container to add the nodes to
     * @param list      The list of items to display
     * @param node      The node to display for each item
     * @param <T>       The type of the items in the list
     * @param <E>       The type of the node
     * @return The For loop
     */
    public static <T, E> For<E, T> node(@NotNull Parent container, @NotNull ObservableList<@NotNull T> list, @NotNull E node) {
        return node(container, list, node, (controller, item) -> {
        });
    }

    private void params(Map<String, Object> params) {
        this.params = params;
    }

    public Parent getContainer() {
        return container;
    }

    public void setContainer(Parent container) {
        this.container = container;
        this.children = Util.getChildrenList(container.getClass(), container);

        init();
    }

    public SimpleObjectProperty<ObservableList<T>> listProperty() {
        return list;
    }

    public ObservableList<T> getList() {
        return list.getValue();
    }

    public void setList(ObservableList<T> list) {
        this.list.setValue(list);

        init();
    }

    public Object getNode() {
        return node;
    }

    /**
     * Sets the node to display for each item.
     * <p>
     * The node can be a Class annotated with @Controller or a Node.
     * <p>
     * If the node is a Class, the controller will be initialized and rendered with the given parameters.
     * <p>
     * If the node is a Node, it will be duplicated and added to the container.
     *
     * @param node The node to display for each item
     */
    @SuppressWarnings("unchecked")
    public void setNode(Object node) {
        this.node = node;

        // If a controller class is provided, use the child controller system to create and initialize a controller
        if (node instanceof Class<?> clazz) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                this.nodeProvider = (item) -> {
                    Object instance = FxFramework.router().getProvidedInstance(clazz);
                    if (initializer != null) {
                        initializer.initialize((E) instance, item);
                    }
                    return FxFramework.router().initAndRender(instance, this.params);
                };
            } else {
                throw new IllegalArgumentException("Class '%s' is not annotated with @Controller. Directly provide a node or use '$fxid' to link a node in FXML.".formatted(clazz.getName()));
            }
        }
        // If a node is provided, duplicate it
        else if (node instanceof Node) {
            if (node.getClass().isAnnotationPresent(Controller.class)) {
                throw new IllegalArgumentException("Node '%s' is annotated with controller. Use the controller's class or a provider if you want to link a controller.".formatted(((Node) node).getId()));
            }
            this.nodeProvider = (item) -> {
                Node duplicated = Duplicators.duplicate((Node) node);
                if (initializer != null) {
                    initializer.initialize((E) duplicated, item);
                }
                return duplicated;
            };
        }
        // If a provider is provided, use the provided element
        else if (node instanceof Provider<?> provider) {
            Object provided = provider.get();
            if (provided instanceof Node element && !element.getClass().isAnnotationPresent(Controller.class)) {
                this.nodeProvider = (item) -> {
                    Node providedNode = (Node) provider.get();
                    if (initializer != null) {
                        initializer.initialize((E) providedNode, item);
                    }
                    return providedNode;
                };
            } else {
                throw new IllegalArgumentException("Provider '%s' does not provide a Node or the Node is annotated with @Controller. Directly provide a node or use '$fxid' to link a node in FXML.".formatted(provider.getClass().getName()));
            }
        }
        // Invalid type
        else {
            throw new IllegalArgumentException("Please provide a Node, a Class annotated with @Controller or a Provider that provides a Node.");
        }

        init();
    }

    /**
     * Binds the list to the container and the node by adding and removing nodes when the list changes.
     * <p>
     * This method is called automatically when the container, list or node is set. You only need to call it manually if you want to rebind the list.
     * <p>
     * Initializes the For loop by adding all existing nodes to the container.
     */
    private void init() {
        if (this.container == null || this.node == null || this.list.isNull().get()) {
            return;
        }

        clearUnused();
        for (T item : this.list.getValue()) {
            Node node = this.nodeProvider.get(item);
            this.nodes.put(item, node);
            this.children.add(node);
        }
    }

    /**
     * Removes the node for the given item from the container.
     *
     * @param item The item to remove
     */
    private void remove(T item) {
        Node node = this.nodes.get(item);
        this.nodes.remove(item);
        this.children.remove(node);
    }

    /**
     * Adds the node for the given item to the container.
     *
     * @param item The item to add
     */
    private void add(T item) {
        if (this.nodes.containsKey(item)) {
            throw new IllegalArgumentException("Item '%s' is already in the list".formatted(item));
        }
        Node node = nodeProvider.get(item);
        nodes.put(item, node);
        children.add(node);
    }

    /**
     * Removes all nodes that are not in the list anymore.
     */
    private void clearUnused() {
        for (T item : this.list.getValue()) {
            if (!nodes.containsKey(item)) {
                children.remove(nodes.get(item));
                nodes.remove(item);
            }
        }
    }

}
