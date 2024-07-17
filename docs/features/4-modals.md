# Modals [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/controller/Modals.html)

Modals are a special type of window that can be used to display a controller on top of another controller. 
Modals can be used to display popup windows, dialogs, etc.

The framework provides a `Modals` class that can be used to display a modal.
Using a builder, a stage can be configured and then be displayed.

An Initializer in the form of a BiConsumer provides access to the component instance and the stage for configuring things the builder doesn't provide.
It can be set using `init()`.
Initializers are called after the component has been created, initialized and rendered, but before the stage is shown.

Enabling `dialog()` adds some default styling to the stage.
This has been added as an option to enable the legacy default styling present in the old modal class.

Parameters can be passed by using the `param()` method in form of a map (see `show()` in the `FulibFxApp` class).

Enabling `destroyOnClose()` configures the component to be destroyed upon closing the modal.
This is enabled by default and shouldn't be changed if not necessary.

The stage can either be built using `build()` and then displayed by yourself, or built and displayed at once using `show()`.

When displaying the component, the parameters `modalStage` and `ownerStage` will be passed so that the modal can for example be closed from inside the component class.

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
Modals modals = new Modals(app); // Can also be injected by Dagger

modals
    .modal(myModalComponent)
    .dialog(true) // Apply the legacy fulibFx styling
    .init((stage, component) -> {
        stage.setTitle(component.getString());    
    })
    .params(Map.of("key", value))
    .show();
```

---

[⬅ History](3-history.md) | [Overview](README.md) | [Duplicator ➡](5-node-duplicator.md)
