# Node duplication

As JavaFX by itself doesn't support the duplication of nodes, the framework implements its own duplication logic in the
form of `Duplicator`s. The framework includes duplicators for most of the
basic JavaFX elements like Buttons, HBoxes, VBoxes and more. If you need to duplicate an element which isn't supported
by default, you can create a custom `Duplicator` and register it in the `Duplicators` class.

```java
Duplicators.register(MyNode.class, (mynode) -> {
    MyNode node = new MyNode();
    node.setText(mynode.getText());
    return node;
});

Duplicators.duplicate(new MyNode("Hello World")); // Returns a new MyNode with the text "Hello World"
```

---

[⬅ Modals](4-modals.md) | [Overview](README.md) | [Data Structures ➡](6-data-structures.md)