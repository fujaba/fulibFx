# ☕ JFX Framework

JFX Framework is a versatile Framework for JavaFX applications that is specifically designed for MVC pattern projects.

## ❓ How to start?

To make use of this framework, you need to create an `App` class (e.g., `TodoApp.class`) that extends the `FxFramework`
class and overrides its `start` method. If you already have a JavaFX App class, you can easily migrate by
changing `extends Application` to `extends FxFramework`.

In the `start` method, you should call `super.start(primaryStage)` to initialize the framework. After that, you can
proceed to set up your routes and controllers.

```java
public class TodoApp extends FxFramework {

    @Override
    public void start(Stage primaryStage) {
        try {

            super.start(primaryStage);

        } catch (Exception e) {
            e.printStackTrace(); // Error logging
        }
    }
}
```

## 🎮 Controllers

Controllers are the backbone of your application. To set up a controller, create a class where the controller logic will
be defined and annotate it with `@Controller`.

```java

@Controller
public class TodoController {

    // Empty constructor (for dependency injection etc.)
    public TodoController() {
    }
}
```

Within your controller class, you have the ability to define methods that are automatically triggered when the
controller is initialized or rendered. These methods should be annotated with either `@ControllerEvent.onInit`
or `@ControllerEvent.onRender` to
specify their respective execution points.

```java

@Controller
public class TodoController {

    // Empty constructor (for dependency injection etc.)
    public TodoController() {
    }

    @ControllerEvent.onInit
    public void thisMethodWillBeCalledOnInit() {
        // Called when the controller is initialized
        System.out.println("Controller initialized");
    }

    @ControllerEvent.onRender
    public void thisMethodWillBeCalledOnRender() {
        // Called when the controller has been loaded and is ready to be displayed
        System.out.println("Controller rendered");
    }
}
```

The initialization of a controller takes place when the controller is created, just before it is fully loaded. During
this phase, you may not have access to elements defined in the corresponding view.

On the other hand, the rendering of a controller occurs when the controller is fully loaded and ready to be displayed.
At this stage, you have full access to all elements defined in the corresponding view.

## 📷 Views

Each controller is associated with a view, which is composed of one or more nested JavaFX elements (panes, buttons,
labels, etc.). You have three different options to define the view of a controller:

### 📎 Using FXML to define the view

Create an FXML file with a name based on the controller class (e.g., `TodoController` --> `todo.fxml`) and place it in
the 'resources' directory at the same path as your app class.

You can also define a custom FXML path to load instead by setting the `view` path in the `@Controller` annotation. Note
that the entered path will always be relative to the path of your app class in the 'resources' directory.

```java

// Leaving 'view' blank will use the default file name (e.g. TodoController --> todo.fxml)
@Controller(view = "view/todo.fxml")
public class TodoController {

    // Empty constructor (for dependency injection etc.)
    public TodoController() {
    }
}
```

When displaying this controller, the framework will automatically load the corresponding FXML file and set it as the
view.

### ☁ Using JavaFX elements to define the view

If the controller class extends from a JavaFX Parent (or any class extending from Parent), the view will be set to the
element the controller represents. You can use this to create simple views without the need for an FXML file. More
complex views should either use the FXML method or combine both methods.

```java

@Controller
public class TodoController extends VBox {

    // Empty constructor (for dependency injection etc.)
    public TodoController() {
        this.getChildren().add(new Label("Hello World"));
    }
}
```

When displaying this controller, the framework will automatically set the view to the VBox element, including all the
modifications like added children, etc.

This method can be combined with the FXML method by extending from a JavaFX Parent and specifying an FXML with
a [root node](https://openjfx.io/javadoc/20/javafx.fxml/javafx/fxml/doc-files/introduction_to_fxml.html#root_elements).
This way you can use the FXML file to define a more complex view and the JavaFX element to add additional
functionality. This will also be very helpful when using the controller as a sub-controller.

### ⚙ Using a method to define the view

If, for some reason, you need special loading logic, you can also define the view by creating a method in the controller
class that returns a JavaFX parent element (e.g., `Pane`, `Button`, `Label`, etc.). This method will be called when the
view is loaded.

You can define the method you want to use by setting the method in the `@Controller` annotation, starting with a '`#`'.

```java

@Controller(view = "#renderThis")
public class TodoController {

    // Empty constructor (for dependency injection etc.)
    public TodoController() {
    }

    public VBox renderThis() {
        return new VBox(new Label("Hello World"));
    }
}
```

## 🌍 Routes

Routes are the way to navigate between views. To set up routes to different views, you have to create a class where the
routes will be defined.

Inside the class, you have to create a field for each route you want to define. The field has to be annotated with
`@Route(route = "...")` and has to be of type `Provider<T>`, where `T` is the controller which should be displayed at
the
route.

If the path of the route isn't specified, the name of the field will be used as the route name.

The example below uses Dagger to inject the controllers into the routing class. If you don't want to use dependency
injection, you can also create the providers manually.

```java

public class Routing {

    @Inject
    @Route(route = "")
    public Provider<MainMenuController> main;

    @Inject
    @Route(route = "/login")
    public Provider<LoginController> login;

    @Inject
    @Route(route = "/login/register")
    public Provider<RegisterController> register;

    @Inject
    @Route // Route name will be '/todo'
    public Provider<TodoController> todo;

    public Routing() {
        // Empty constructor (for dependency injection etc.)
    }

}
```

This setup will result in the following routing tree:

<img src=".github/assets/route-diagram.png" height="300" alt="Routing tree showing main, login, todo and register routes in a tree like structure">

## 🖥 Displaying controllers

To display a controller, you have to call the `show()` method of the `FxFramework` class and pass the route.

```java
public class TodoApp extends FxFramework {
    @Override
    public void start(Stage primaryStage) {
        try {

            super.start(primaryStage);
            show(""); // Start in the Main Menu

        } catch (Exception e) {
            e.printStackTrace(); // Error logging
        }
    }
}
```

The route works like a file system and is therefore relative to the currently displayed controller if it doesn't start
with a '`/`'. If you want to display a controller from the root, you have to start the route with a '`/`'. The route
also
supports path traversal (e.g., '`../login`'). This can be used to create a back button.

```java

@Controller
public class TodoController {

    @FXML
    Button backButton; // Button is specified in the FXML file

    @FXML
    Button todoButton; // Button is specified in the FXML file

    // Empty constructor (for dependency injection etc.)
    public TodoController() {
    }

    @ControllerEvent.onRender
    public void addButtonAction() {
        this.backButton.setOnAction(event -> show("../"));
        this.todoButton.setOnAction(event -> show("/todo"));
    }
}
```

## 🧵 Sub-Controllers

Controllers can be used inside other controllers to create reusable components. This can be done by using the
`@Providing` annotation on a field of type `Provider<T>` in your routing class, where `T` is the controller which should
be displayed. You only need on providing field per controller, even if you want to display it multiple times.

In order to display a sub-controller, open the FXML file of the parent controller and add the sub-controller as an
element like this:

```xml
<?import io.github.sekassel.jfxexample.controller.TodoController?>
        ...
<TodoController fx:id="yourid" onAction="onActionMethod" ... />
```

Depending on the parent you extended from, all attributes/properties available for this parent can be set for your
custom controller element as well.

## 🔁 For-Loops

For-Loops can be used to easily display a node/sub-controller for all items in a list. Whenever an item is added to or 
removed from the list, the list of nodes updates accordingly.

The easiest form of a For-Loop can be achieved like this:

```java
For.controller(container, items, ExampleController.class);
```

This will create an `ExampleController` for each item in the list `items` and add it to the children of the `container` (e.g. a VBox).

Currently, no information is passed to the created controller. In order to pass static information you can add parameters like you would 
when using the `show`-method using a map. 

```java
For.controller(container, items, ExampleController.class, Map.of("key", value));
For.controller(container, items, ExampleController.class, params); // Parameters can be taken from the @Params annotation for example
```

If you want to pass dynamic information like binding the item to its controller, you can use an `Initializer`. The `Initializer` allows to 
define actions for initializing each controller based on its item.

```java
For.controller(container, items, ExampleController.class, (controller, item) -> {
    controller.setItem(item);
    controller.foo();
    controller.bar();
});

For.controller(container, items, ExampleController.class, ExampleController::setItem); // Short form using method references
For.controller(container, items, ExampleController.class, Map.of("key", value), ExampleController::setItem); // Static and dynamic information can be passed together
```

Instead of a controller you can also define a basic JavaFX node to display for every item.

```java
For.node(container, items, new Button("This is a button!"));
```

When using nodes, the framework will create a copy of the provided node to display for every item. The copies usually contain all required information except for bindings.

```java
For.node(container, items, new Button("This is a button!"));
For.node(container, items, new VBox(new Button("This is a button!"))); // Nodes can have children
```

Unlike with controllers, it is not possible to pass static information in the form of paramters to nodes, as there is no way of accessing them in the code. However, dynamic
information in the form of an `Initializer` can be used just like with controllers.

```java
For.node(container, items, new Button(), (button, item) -> {
    button.setText("Delete " + item.name();
    button.setOnAction(event -> items.remove(item));
});
```

As JavaFX by itself doesn't support the duplication of nodes, the framework implements its own duplication logic in the form of `Duplicator`s. The framework includes duplicators for most of the
basic JavaFX elements like Buttons, HBoxes, VBoxes and more. If you need to duplicate an element which isn't supported by default, you can create a custom `Duplicator` and register it in the `Duplicators` class.

## ↘ Call order

Since the framework differentiates between initialization and rendering, different methods annotated with `@ControllerEvent.onInit` or `@ControllerEvent.onRender` are called at different times.

The framework has a general rule of **initialization before rendering**, meaning you cannot access most JavaFX elements (for example nodes defined in an FXML file) in the init methods as the elements aren't loaded before the rendering.

When using sub-controllers or For-Loops, the order of operations is a bit more complex. At first the main controller will be initialized. After the controller has been initialized, all sub-controllers will be loaded and therefore initialized. This will happen recursively until a sub-controller doesn't have any sub-controllers. After that, the sub-controllers will be rendered, going back up to the main controller. The main controller will be rendered, after all the sub-controllers have been rendered.

If a For-Loop is defined in a method annotated with `@Controller.onRender` in any (sub-)controller, the `@Controller.onRender` will (obviously) be called first. After that, the `Initializer` of the for-Controller will be called and then the for-controller will be initialized and then rendered.

#### Example

```java
@Controller()
public class Controller {

    // Constructor, elements etc.
    // This controller has a subcontroller defined in the FXML file

    @ControllerEvent.onInit
    public void onInit() {
        System.out.println("onInit Controller");
    }

    @ControllerEvent.onRender 
    public void onRender() {
        System.out.println("onRender Controller");
        For.controller(container, items, ForController.class, (controller, item) -> System.out.println("Initializer ForController"));
    }
    
}
```

```java
@Controller()
public class SubController {

    // Constructor, elements etc.
    // This controller has another subcontroller defined in the FXML file

    @ControllerEvent.onInit
    public void onInit() {
        System.out.println("onInit SubController");
    }

    @ControllerEvent.onRender 
    public void onRender() {
        System.out.println("onRender SubController");
    }
    
}
```

```java
@Controller()
public class ForController {

    // Constructor, elements etc.

    @ControllerEvent.onInit
    public void onInit() {
        System.out.println("onInit ForController");
    }

    @ControllerEvent.onRender 
    public void onRender() {
        System.out.println("onRender ForController");
    }
    
}
```

This setup results in the following outputs:

```
Constructor Controller
onInit Controller
Constructor SubController
onInit SubController
Constructor SubSubController
onInit SubSubController
onRender SubSubController
onRender SubController
Constructor ForController
Initializer ForController
onRender Controller
onInit ForController
onRender ForController
```
