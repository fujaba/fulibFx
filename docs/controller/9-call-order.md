# Call order

Since the framework differentiates between initialization and rendering, different methods annotated
with `@onInit` or `@onRender` are called at different times.

The framework has a general rule of **initialization before rendering**, meaning you cannot access most JavaFX
elements (for example nodes defined in an FXML file) in the init methods as the elements aren't loaded before the
rendering.

When using subcomponents or [For-Loops](../features/2-for), the order of operations is a bit more complex. At first the main controller
will be initialized. After the controller has been initialized, all subcomponents will be loaded and therefore
initialized. This will happen recursively until a subcomponent doesn't have any subcomponents. After that, the
subcomponents will be rendered, going back up to the main controller. The main controller will be rendered, after all
the subcomponents have been rendered.

If a For-Loop is defined in a method annotated with `onRender` in any (sub-)controller,
the `onRender` will (obviously) be called first. After that, the initializer (`BiConsumer`) of the
for-Controller will be called and then the for-controller will be initialized and then rendered.

#### Example

```java

@Controller(view = "Example.fxml")
public class ExampleController {

    // Constructor, elements etc.
    // This controller has a subcomponent defined in the FXML file

    @onInit
    public void onInit() {
        System.out.println("onInit Controller");
    }

    @onRender
    public void onRender() {
        System.out.println("onRender Controller");
        fxFor.create(container, items, forControllerProvider, (controller, item) -> System.out.println("Initializer ForController"));
    }

}
```

```java

@Component(view = "Sub.fxml")
public class SubController extends VBox {

    // Constructor, elements etc.
    // This controller has another subcomponent defined in the FXML file

    @onInit
    public void onInit() {
        System.out.println("onInit SubController");
    }

    @onRender
    public void onRender() {
        System.out.println("onRender SubController");
    }

}
```

```java

@Component
public class ForController extends VBox {

    // Constructor, elements etc.

    @onInit
    public void onInit() {
        System.out.println("onInit ForController");
    }

    @onRender
    public void onRender() {
        System.out.println("onRender ForController");
    }

}
```

This setup results in the following outputs:

```
Constructor ExampleController
onInit ExampleController
Constructor SubController
onInit SubController
Constructor SubSubController
onInit SubSubController
onRender SubSubController
onRender SubController
onRender ExampleController
Constructor ForController
Initializer ForController
onInit ForController
onRender ForController
```

The destruction happens in the reverse order of the rendering.