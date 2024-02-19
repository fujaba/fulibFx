package org.fulib.fx.constructs;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.controller.ControllerManager;
import org.fulib.fx.util.ControllerUtil;
import org.fulib.fx.util.ReflectionUtil;
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
 * @param <Node> The type of the node to display for each item (e.g. a component or a node)
 * @param <Item> The type of the items in the list
 */
public class For<Node extends javafx.scene.Node, Item> {

    // The nodes that are currently displayed for each item
    private final HashMap<Item, Node> itemsToNodes = new HashMap<>();

    // The disposable that is used to destroy the For loop and all controllers
    CompositeDisposable disposable;

    // List of items to iterate over
    private ObservableList<Item> items;
    // The provider to create the node/component for each item
    private Provider<Node> provider;
    // The parameters to pass to the controller
    private Map<String, Object> params;
    // The method to call when the controller is created
    private BiConsumer<Node, Item> beforeInit;

    // The container to add the nodes to (the nodes will be added to the children of the container)
    private Parent container;
    // The children of the container (saved for performance)
    private ObservableList<javafx.scene.Node> children;

    // Listener to add to the list to update the order of the children when the list changes
    ListChangeListener<Item> listChangeListener = change -> {
        while (change.next()) {
            if (change.wasPermutated()) {
                // Update the order of the children
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    children.set(i, children.set(change.getPermutation(i), children.get(i)));
                }
            }
            if (change.wasRemoved()) {
                for (Item item : change.getRemoved()) {
                    remove(item);
                }
            }
            if (change.wasAdded()) {
                int i = 0;
                // Add the new items in the correct order
                for (Item item : change.getAddedSubList()) {
                    add(item, change.getFrom() + i++);
                }
            }
        }
    };

    /**
     * Use the factory methods to create a new For loop.
     */
    private For() {

        // This will be called when the For loop is destroyed. Controllers will be added to the disposable automatically.
        this.disposable().add(Disposable.fromRunnable(() -> {

            if (items != null) {
                // Clear all listeners
                this.items.removeListener(listChangeListener);
                // Remove all nodes (destroys any remaining controllers)
                this.items.forEach(this::remove);
            }

            // Cleanup
            this.children = null;
            this.itemsToNodes.clear();
            this.children = null;
            this.items = null;

            this.provider = null;
            this.params = null;
            this.container = null;
        }));
    }

    /**
     * Creates a new For loop for use in code and initializes it.
     * <p>
     * This factory will use the provider to create a new node for each item in the list.
     * The nodes will be added to the children of the container.
     * If the provided type is a {@link Component}, the controller will be initialized and rendered.
     * <p>
     * Example: For.of(myVbox, myListOfItems, myControllerProvider, Map.of("argument", value), (controller, item) -> controller.setItem(item));
     *
     * @param container    The container to add the nodes to
     * @param items        The list of items to display
     * @param nodeProvider The provider to create the controller for each item
     * @param beforeInit   The method to call when the controller is created (useful for setting the item)
     * @param params       The parameters to pass to the created controller
     * @param <Item>       The type of the items in the list
     * @param <Node>       The type of the node
     * @return The For loop
     */
    public static <Node extends javafx.scene.Node, Item> For<Node, Item> of(@NotNull Parent container, @NotNull ObservableList<@NotNull Item> items, @NotNull Provider<@NotNull Node> nodeProvider, @NotNull Map<@NotNull String, @Nullable Object> params, @NotNull BiConsumer<@NotNull Node, @Nullable Item> beforeInit) {
        For<Node, Item> forLoop = new For<>();
        forLoop.beforeInit = beforeInit;
        forLoop.setContainer(container);
        forLoop.setItems(items);
        forLoop.setProvider(nodeProvider);
        forLoop.setParams(params);
        forLoop.init();
        return forLoop;
    }

    /**
     * Creates a new For loop for use in code and initializes it.
     * <p>
     * This factory will use the provider to create a new node for each item in the list.
     * The nodes will be added to the children of the container.
     * If the provided type is a {@link Component}, the controller will be initialized and rendered.
     * <p>
     * Example: For.of(myVbox, myListOfItems, myControllerProvider, (controller, item) -> controller.setItem(item));
     *
     * @param container    The container to add the nodes to
     * @param items        The list of items to display
     * @param nodeProvider The provider to create the controller for each item
     * @param beforeInit   The method to call when the controller is created (useful for setting the item)
     * @param <Item>       The type of the items in the list
     * @param <Node>       The type of the node
     * @return The For loop
     */
    public static <Node extends javafx.scene.Node, Item> For<Node, Item> of(@NotNull Parent container, @NotNull ObservableList<@NotNull Item> items, @NotNull Provider<@NotNull Node> nodeProvider, @NotNull BiConsumer<@NotNull Node, @Nullable Item> beforeInit) {
        return of(container, items, nodeProvider, Map.of(), beforeInit);
    }

    /**
     * Creates a new For loop for use in code and initializes it.
     * <p>
     * This factory will use the provider to create a new node for each item in the list.
     * The nodes will be added to the children of the container.
     * If the provided type is a {@link Component}, the controller will be initialized and rendered.
     * <p>
     * Example: For.of(myVbox, myListOfItems, () -> new Button());
     * <p>
     * Example: For.of(myVbox, myListOfItems, myControllerProvider);
     *
     * @param container    The container to add the nodes to
     * @param items        The list of items to display
     * @param nodeProvider The provider to create the controller for each item
     * @param <Item>       The type of the items in the list
     * @param <Node>       The type of the node
     * @return The For loop
     */
    public static <Node extends javafx.scene.Node, Item> For<Node, Item> of(@NotNull Parent container, @NotNull ObservableList<@NotNull Item> items, @NotNull Provider<@NotNull Node> nodeProvider) {
        return of(container, items, nodeProvider, Map.of(), (controller, item) -> {
        });
    }

    private void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Parent getContainer() {
        return container;
    }

    private void setContainer(Parent container) {
        this.container = container;
        this.children = ReflectionUtil.getChildrenList(container.getClass(), container);

        init();
    }

    public ObservableList<Item> getItems() {
        return FXCollections.unmodifiableObservableList(this.items);
    }

    private void setItems(ObservableList<Item> list) {
        this.items = list;
    }

    public Provider<Node> getProvider() {
        return provider;
    }

    private void setProvider(Provider<Node> node) {
        this.provider = node;

    }

    /**
     * Binds the list to the container and the node by adding and removing nodes when the list changes.
     * <p>
     * This method is called automatically when the container, list or node is set. You only need to call it manually if you want to rebind the list.
     * <p>
     * Initializes the For loop by adding all existing nodes to the container.
     */
    private void init() {
        if (this.container == null || this.provider == null || this.items == null) {
            return;
        }

        this.items.addListener(listChangeListener);

        clearUnused();
        for (int i = 0; i < this.items.size(); i++) {
            add(this.items.get(i), i);
        }
    }

    /**
     * Removes the node for the given item from the container.
     *
     * @param item The item to remove
     */
    private void remove(Item item) {
        Node node = this.itemsToNodes.get(item);

        // Destroy the controller if the node is a component
        if (ControllerUtil.isComponent(node)) {
            ControllerManager.destroy(node);
        }

        // Remove the node from the container
        this.itemsToNodes.remove(item);
        this.children.remove(node);
    }

    /**
     * Adds the node for the given item to the container.
     *
     * @param item  The item to add
     * @param index The index to add the item at
     */
    private void add(Item item, int index) {
        if (this.itemsToNodes.containsKey(item)) {
            throw new IllegalArgumentException("Item '%s' is already in the list".formatted(item));
        }

        // Create the node
        Node node = provider.get();

        // If logic is needed before the controller is initialized, call the method
        if (beforeInit != null) {
            beforeInit.accept(node, item);
        }

        // Initialize and render the controller if the node is a component
        if (ControllerUtil.isComponent(node)) {
            ControllerManager.init(node, params);
            ControllerManager.render(node, params);
        }

        // Add the node to the container
        itemsToNodes.put(item, node);
        children.add(index, node);
    }

    /**
     * Removes all nodes from the container which have no corresponding item in the list.
     */
    private void clearUnused() {
        for (Item item : this.items) {
            if (!itemsToNodes.containsKey(item)) {
                children.remove(itemsToNodes.get(item));
                itemsToNodes.remove(item);
            }
        }
    }

    /**
     * Returns the disposable used for cleaning up the For loop.
     * This
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
