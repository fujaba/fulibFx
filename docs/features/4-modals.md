# Modals [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/controller/Modals.html)

Modals are a special type of window that can be used to display a controller on top of another controller. Modals
can be used to display popup windows, dialogs, etc.

The framework provides a `Modals` class that can be used to display a modal.

```java
Modals.showModal(app, confirmComponent, (stage, component) -> {
    stage.setTitle("Modal");
    component.setConfirmAction(() -> {
        // Do something
        stage.close();
    });
});

```

---

[⬅ History](3-history.md) | [Overview](README.md) | [Duplicator ➡](5-node-duplicator.md)