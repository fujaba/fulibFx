package org.fulib.fx.constructs;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import org.fulib.fx.controller.ControllerManager;
import org.fulib.fx.util.ControllerUtil;
import org.fulib.fx.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

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
    private Map<Item, Node> itemsToNodes;

    // The disposable that is used to destroy the For loop and all controllers
    private CompositeDisposable disposable;

    // The controller manager to create and destroy controllers
    private final ControllerManager controllerManager;

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
    private final ListChangeListener<Item> listChangeListener = change -> {
        while (change.next()) {
            if (change.wasPermutated()) {
                // Update the order of the children
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    this.children.set(i, children.set(change.getPermutation(i), this.children.get(i)));
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
    protected For(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;
        // This will be called when the For loop is destroyed. Controllers will be added to the disposable automatically.
        this.disposable().add(Disposable.fromRunnable(this::cleanup));
    }

    /**
     * Cleans up the For loop by removing all listeners and nodes.
     */
    private void cleanup() {
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
    }

    protected void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * Returns the container to which the nodes are added.
     *
     * @return The container
     */
    public Parent getContainer() {
        return container;
    }

    protected void setContainer(Parent container) {
        this.container = container;
        this.children = ReflectionUtil.getChildrenList(container.getClass(), container);

        init();
    }

    public ObservableList<Item> getItems() {
        return FXCollections.unmodifiableObservableList(this.items);
    }

    protected void setItems(ObservableList<Item> list) {
        this.items = list;
    }

    public Provider<Node> getProvider() {
        return provider;
    }

    protected void setProvider(Provider<Node> node) {
        this.provider = node;
    }

    protected void setBeforeInit(BiConsumer<Node, Item> beforeInit) {
        this.beforeInit = beforeInit;
    }

    /**
     * Binds the list to the container and the node by adding and removing nodes when the list changes.
     * <p>
     * This method is called automatically when the container, list or node is set. You only need to call it manually if you want to rebind the list.
     * <p>
     * Initializes the For loop by adding all existing nodes to the container.
     */
    protected void init() {
        if (this.container == null || this.provider == null || this.items == null) {
            return;
        }

        if (this.itemsToNodes != null)
            throw new IllegalStateException("For loop is already initialized!");

        this.itemsToNodes = new HashMap<>();

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
            controllerManager.destroy(node);
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
        Node node = this.provider.get();

        // If logic is needed before the controller is initialized, call the method
        if (this.beforeInit != null) {
            this.beforeInit.accept(node, item);
        }

        // Initialize and render the controller if the node is a component
        if (ControllerUtil.isComponent(node)) {
            controllerManager.init(node, this.params);
            controllerManager.render(node, this.params);
        }

        // Add the node to the container
        this.itemsToNodes.put(item, node);
        this.children.add(index, node);
    }

    /**
     * Removes all nodes from the container which have no corresponding item in the list.
     */
    private void clearUnused() {
        for (Item item : this.items) {
            if (!this.itemsToNodes.containsKey(item)) {
                this.children.remove(this.itemsToNodes.get(item));
                this.itemsToNodes.remove(item);
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
        if (this.disposable == null || this.disposable.isDisposed()) {
            this.disposable = new CompositeDisposable();
        }
        return this.disposable;
    }

    /**
     * Destroys the For loop and all controllers.
     */
    public void dispose() {
        if (this.disposable != null) {
            this.disposable.dispose();
        }
    }
}
