# Modals [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/controller/Modals.html)

Modals are a special type of window that can be used to display a controller on top of another controller. Modals
can be used to display popup windows, dialogs, etc.

The framework provides a `Modals` class that can be used to display a modal.
When using `showModal()` a stage will be created and configured to be displayed above the current stage.
A BiConsumer provides access to the component instance and the stage for configuring other things.

When displaying the component, the parameters `modalStage` and `ownerStage` will be passed so that the modal can for
example be closed from inside the component class.

The same component instance cannot be used twice for displaying a modal. When using Dagger it is recommended to inject a provider and use it to create an instance for every modal.

```java

@Component
public class ModalComponent extends VBox {

    // ...

    @Param("modalStage")
    Stage modal;

    @OnRender
    void onRender() {
        modal.setTitle("Modal");
    }

    @FXML
    void onCloseClick() {
        doStuff();
        modal.close();
    }

}
```

```java
// As every modal needs its own instance, we use a provider (e.g. with Dagger)
Modals.showModal(app, modalComponentProvider.get(), (stage, component) -> {
    stage.doSomething();
    component.doSomethingElse();
});
```

---

[⬅ History](3-history.md) | [Overview](README.md) | [Duplicator ➡](5-node-duplicator.md)
