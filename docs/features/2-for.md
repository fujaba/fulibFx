# For-Loops

For-Loops can be used to easily display a node/sub-controller for all items in a list. Whenever an item is added to or
removed from the list, the list of nodes updates accordingly.

In order to create a for loop, acquire an instance of the `FxFor` class (e.g. using Dagger) and call the `of` method.
The easiest form of a For-Loop can be achieved like this:

```java
fxFor.of(container, items, myComponentProvider);
```

This will create a component for each item in the list `items` and add it to the children of the `container` (e.g. a VBox).

Currently, no information is passed to the created label. In order to pass static information you can add
parameters like you would when using the `show`-method using a map.

```java
fxFor.of(container, items, myComponentProvider, Map.of("key", value));
fxFor.of(container, items, myComponentProvider, params); // Parameters can be taken from the @Params annotation for example
```

If you want to pass dynamic information like binding the item to its controller, you can use an `BiConsumer`.
The `BiConsumer` allows to define actions for initializing each controller based on its item.

```java
fxFor.of(container, items, myComponentProvider, (controller, item) -> {
    controller.setItem(item);
    controller.foo();
    controller.bar();
});

fxFor.of(container, items, myComponentProvider, ExampleComponent::setItem); // Short form using method references
fxFor.of(container, items, myComponentProvider, Map.of("key", value), ExampleComponent::setItem); // Static and dynamic information can be passed together
```

Instead of a controller you can also define a basic JavaFX node to display for every item.

```java
fxFor.of(container, items, () -> new Button("This is a button!"));
fxFor.of(container, items, () -> new VBox(new Button("This is a button!"))); // Nodes can have children
```

Unlike with controllers, it is not possible to pass static information in the form of paramters to nodes, as there is no
way of accessing them in the code. However, dynamic information in the form of an `BiConsumer` can be used just like with
controllers.

```java
fxFor.of(container, items, () -> new Button(), (button, item) -> {
    button.setText("Delete " + item.name());
    button.setOnAction(event -> items.remove(item));
});
```

In order to destroy controllers generated by the For-loops, you can use the `dispose()` method of the `For` class or add
the return value of the `disposable()` method to your list of disposables.