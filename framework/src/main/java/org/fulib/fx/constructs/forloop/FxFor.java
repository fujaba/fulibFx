package org.fulib.fx.constructs.forloop;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.controller.ControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Factory class for {@link For} loops.
 */
@Singleton
public class FxFor {

    private final ControllerManager controllerManager;

    @Inject
    public FxFor(ControllerManager controllerManager) {
        this.controllerManager = controllerManager;
    }

    /**
     * Creates a new For loop for use in code and initializes it.
     * <p>
     * This factory will use the provider to create a new node for each item in the list.
     * The nodes will be added to the children of the container.
     * If the provided type is a {@link Component}, it will be initialized and rendered.
     * <p>
     * Example: For.of(myVbox, myListOfItems, myControllerProvider, Map.of("argument", value), (controller, item) -> controller.setItem(item));
     *
     * @param container    The container to add the nodes to
     * @param items        The list of items to display
     * @param nodeProvider The provider to create the controller for each item
     * @param beforeInit   The method to call when the controller is created (useful for setting the item)
     * @param params       The parameters to pass to the created controller
     * @param <Item>       The type of item
     * @param <Node>       The node to display for each item
     * @return The For loop
     */
    public <Node extends javafx.scene.Node, Item> For<Node, Item> of(@NotNull Parent container, @NotNull ObservableList<@NotNull Item> items, @NotNull Provider<@NotNull Node> nodeProvider, @NotNull Map<@NotNull String, @Nullable Object> params, @NotNull BiConsumer<@NotNull Node, @Nullable Item> beforeInit) {
        For<Node, Item> forLoop = new For<>(this.controllerManager);
        forLoop.setBeforeInit(beforeInit);
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
     * @param <Item>       The type of item
     * @param <Node>       The node to display for each item
     * @return The For loop
     */
    public <Node extends javafx.scene.Node, Item> For<Node, Item> of(@NotNull Parent container, @NotNull ObservableList<@NotNull Item> items, @NotNull Provider<@NotNull Node> nodeProvider, @NotNull BiConsumer<@NotNull Node, @Nullable Item> beforeInit) {
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
     * @param <Item>       The type of item
     * @param <Node>       The node to display for each item
     * @return The For loop
     */
    public <Node extends javafx.scene.Node, Item> For<Node, Item> of(@NotNull Parent container, @NotNull ObservableList<@NotNull Item> items, @NotNull Provider<@NotNull Node> nodeProvider) {
        return of(container, items, nodeProvider, Map.of(), (controller, item) -> {
        });
    }

}