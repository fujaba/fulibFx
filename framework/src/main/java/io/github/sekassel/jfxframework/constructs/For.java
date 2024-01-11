package io.github.sekassel.jfxframework.constructs;

import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.annotation.controller.Component;
import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.duplicate.Duplicators;
import io.github.sekassel.jfxframework.util.ArgumentProvider;
import io.github.sekassel.jfxframework.util.Util;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A For loop for use in Code. Creates a node for each item in a list and adds them to the children of a container.
 * This allows you to easily display a list of items in a container like a friend list or a list of products.
 *
 * @param <N> The type of the node
 * @param <I> The type of the items in the list
 */
// TODO: sorted lists (order controllers the same way as the items)
public class For<N, I> extends Parent {

    // List of items to iterate over
    private final SimpleObjectProperty<ObservableList<I>> list = new SimpleObjectProperty<>();
    // The nodes that are currently displayed for each item
    private final HashMap<I, Node> itemsToNodes = new HashMap<>();
    // The disposable that is used to destroy the For loop and all controllers
    CompositeDisposable disposable;
    // The node to display for each iteration
    private Object node;
    // The container to add the nodes to
    private Parent container;
    // The provider to create the node for each item
    private ArgumentProvider<Node, I> nodeProvider;
    // The parameters to pass to the controller
    private Map<String, Object> params;
    // The children of the container (saved for performance)
    private ObservableList<Node> children;

    // Listener to add to the list to update the order of the children when the list changes
    ListChangeListener<I> listChangeListener = change -> {
        while (change.next()) {
            if (change.wasPermutated()) {
                // Update the order of the children
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    children.set(i, children.set(change.getPermutation(i), children.get(i)));
                }
            }
            if (change.wasRemoved()) {
                for (I item : change.getRemoved()) {
                    remove(item);
                }
            }
            if (change.wasAdded()) {
                int i = 0;
                // Add the new items in the correct order
                for (I item : change.getAddedSubList()) {
                    add(item, change.getFrom() + i++);
                }
            }
        }
    };

    // Listener to the list property to update the children when the list changes
    ChangeListener<ObservableList<I>> listPropertyListener = (observable, oldValue, newValue) -> {
        if (oldValue != null) {
            oldValue.removeListener(listChangeListener);
        }
        if (newValue != null) {
            newValue.addListener(listChangeListener);
        }
    };

    // The method to call when the controller is created
    private BiConsumer<N, I> beforeInit;

    /**
     * Use the factory methods to create a new For loop.
     */
    private For() {
        list.addListener(listPropertyListener);

        // This will be called when the For loop is destroyed. Controllers will be added to the disposable automatically.
        this.disposable().add(Disposable.fromRunnable(() -> {

            // Clear all listeners
            if (!list.isNull().get()) {
                this.list.getValue().removeListener(listChangeListener);
                this.list.setValue(null);
            }
            this.list.removeListener(listPropertyListener);

            // Cleanup
            this.children.clear();
            this.itemsToNodes.clear();
            this.children = null;

            this.node = null;
            this.nodeProvider = null;
            this.params = null;
            this.container = null;

        }));
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
     * @param container  The container to add the nodes to
     * @param list       The list of items to display
     * @param node       The node to display for each item
     * @param params     The parameters to pass to the created controller
     * @param beforeInit The method to call when the controller is created (useful for setting the item)
     * @param <I>        The type of the items in the list
     * @param <N>        The type of the node
     * @return The For loop
     */
    public static <N, I> For<N, I> controller(@NotNull Parent container, @NotNull ObservableList<@NotNull I> list, @NotNull Class<? extends N> node, @NotNull Map<@NotNull String, @Nullable Object> params, @NotNull BiConsumer<@NotNull N, @Nullable I> beforeInit) {
        For<N, I> forLoop = new For<>();
        forLoop.beforeInit = beforeInit;
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
     * @param <I>       The type of the items in the list
     * @param <N>       The type of the node
     * @return The For loop
     */
    public static <N, I> For<N, I> controller(@NotNull Parent container, @NotNull ObservableList<@NotNull I> list, @NotNull Class<? extends N> node) {
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
     * @param container  The container to add the nodes to
     * @param list       The list of items to display
     * @param node       The node to display for each item
     * @param beforeInit The method to call when the controller is created (useful for setting the item)
     * @param <I>        The type of the items in the list
     * @param <N>        The type of the node
     * @return The For loop
     */
    public static <N, I> For<N, I> controller(@NotNull Parent container, @NotNull ObservableList<@NotNull I> list, @NotNull Class<? extends N> node, @NotNull BiConsumer<@NotNull N, @Nullable I> beforeInit) {
        return controller(container, list, node, Map.of(), beforeInit);
    }

    /**
     * Creates a new For loop for use in Code and initializes it.
     * <p>
     * This factory is based on Nodes. It will duplicate the given node for each item in the list.
     * <p>
     * Example: For.create(myVbox, myListOfItems, new Label(), (label, item) -> label.setText(item.getName()));
     *
     * @param container  The container to add the nodes to
     * @param list       The list of items to display
     * @param node       The node to display for each item
     * @param beforeInit The method to call when the controller is created (useful for setting the item)
     * @param <I>        The type of the items in the list
     * @param <N>        The type of the node
     * @return The For loop
     */
    public static <I, N> For<N, I> node(@NotNull Parent container, @NotNull ObservableList<@NotNull I> list, @NotNull N node, @NotNull BiConsumer<@NotNull N, @Nullable I> beforeInit) {
        For<N, I> forLoop = new For<>();
        forLoop.beforeInit = beforeInit;
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
     * @param <I>       The type of the items in the list
     * @param <N>       The type of the node
     * @return The For loop
     */
    public static <N, I> For<N, I> node(@NotNull Parent container, @NotNull ObservableList<@NotNull I> list, @NotNull N node) {
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

    public SimpleObjectProperty<ObservableList<I>> listProperty() {
        return list;
    }

    public ObservableList<I> getList() {
        return list.getValue();
    }

    public void setList(ObservableList<I> list) {
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
            if (clazz.isAnnotationPresent(Component.class)) {
                this.nodeProvider = (item) -> {
                    Object instance = FxFramework.framework().frameworkComponent().router().getProvidedInstance(clazz);
                    this.disposable().add(Disposable.fromRunnable(() -> FxFramework.framework().manager().destroy(instance)));
                    if (beforeInit != null) {
                        beforeInit.accept((N) instance, item);
                    }
                    FxFramework.framework().frameworkComponent().controllerManager().init(instance, this.params);
                    return FxFramework.framework().frameworkComponent().controllerManager().render(instance, this.params);
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
                if (beforeInit != null) {
                    beforeInit.accept((N) duplicated, item);
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
                    if (beforeInit != null) {
                        beforeInit.accept((N) providedNode, item);
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
        for (I item : this.list.getValue()) {
            Node node = this.nodeProvider.get(item);
            this.itemsToNodes.put(item, node);
            this.children.add(node);
        }
    }

    /**
     * Removes the node for the given item from the container.
     *
     * @param item The item to remove
     */
    private void remove(I item) {
        Node node = this.itemsToNodes.get(item);
        this.itemsToNodes.remove(item);
        this.children.remove(node);
    }

    /**
     * Adds the node for the given item to the container.
     *
     * @param item  The item to add
     * @param index The index to add the item at
     */
    private void add(I item, int index) {
        if (this.itemsToNodes.containsKey(item)) {
            throw new IllegalArgumentException("Item '%s' is already in the list".formatted(item));
        }
        Node node = nodeProvider.get(item);
        itemsToNodes.put(item, node);
        children.add(index, node);
    }

    /**
     * Removes all nodes that are not in the list anymore.
     */
    private void clearUnused() {
        for (I item : this.list.getValue()) {
            if (!itemsToNodes.containsKey(item)) {
                children.remove(itemsToNodes.get(item));
                itemsToNodes.remove(item);
            }
        }
    }

    /**
     * Returns the disposable that is used to destroy the For loop and all controllers.
     *
     * @return The disposable
     */
    public @NotNull CompositeDisposable disposable() {
        if (disposable == null || disposable.isDisposed()) {
            disposable = new CompositeDisposable();
        }
        return disposable;
    }

    /**
     * Destroys the For loop and all controllers.
     */
    public void dispose() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

}
