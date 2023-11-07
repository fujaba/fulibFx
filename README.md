# ☕ JFX Framework

Framework for JavaFX applications focused on MVC pattern projects.

## ❓ How to start?

In order to use this framework you need to create an App class (e.g `Todo.class`) extending the `FxFramework` class and
implement the `start` method. If you already have a JavaFX App class, you can just change `extends Application`
to `extends FxFramework`.

In the `start` method you should call `super.start(primaryStage)` in order to initialize the framework. After that, you
can start with setting up your routes and controllers.

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

Controllers are the way to control the things you see in the application. In order to set up a controller, you have to
create a class where
the controller mechanics will be defined and annotate it with `@Controller`.

```java

@Controller
public class TodoController {

    // Empty constructor (for dependency injection etc.)
    public TodoController() {
    }
}
```

## 📷 Views

Every controller has a corresponding view which consists of one or multiple nested JavaFX elements (panes, buttons,
labels, ...). In order to define the view of a controller, you have three different options:

### 📎 Using FXML to define the view

Create an FXML file with name based on the controller class (e.g. `TodoController` --> `todo.fxml`) and put it in the '
resources' directory at the same path as your app class.

You can also define a custom FXML path to load instead by setting the view path in the `@Controller` annotation.
Note that the entered path will always be relative to the path of your app class in the 'resources' directory.

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
element the controller represents.
You can use this to create simple views without the need of an FXML file. More complex views should either use the FXML
method or combine both methods.

```java

@Controller
public class TodoController extends VBox {

    // Empty constructor (for dependency injection etc.)
    public TodoController() {
        this.getChildren().add(new Label("Hello World"));
    }
}
```

When displaying this controller, the framework will automatically set the view to the VBox element including all the
modifications like added children etc.

This method can be combined with the FXML method by extending from a JavaFX Parent and specifying an FXML with
a [root node](https://openjfx.io/javadoc/20/javafx.fxml/javafx/fxml/doc-files/introduction_to_fxml.html#root_elements).
This way you can use the FXML file to define a more complex view and the JavaFX element to add additional functionality.
This will also be very helpful when using the controller as a sub-controller.

### ⚙ Using a method to define the view

If for some reason you need special loading logic, ou can also define the view by creating a method in the controller
class which returns a JavaFX parent element (e.g. `Pane`, `Button`, `Label`, ...). This method will be called when the
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

Routes are the way to navigate between views. In order to set up routes to different views, you have to create a class
where the routes will be defined.

Inside of the class you have to create a field for each route you want to define. The field has to be annotated with
`@Route(route = "...")` and has to be of type `Provider<T>`, where `T` is the controller which should be displayed at
the route.

If the path of the route isn't specified, the name of the field will be used as the route name.

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

In order to display a controller, you have to call the `show()` method of the `FxFramework` class and pass the route.

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
with a '`/`'. If you want to display a controller from the root, you have to start the route with a '`/`'.
The route also supports path traversal (e.g. `../login`). This can be used to create a back button.

```java

@Controller
public class TodoController {

    @FXML
    Button backButton; // Button is specified in the FXML file

    @FXML
    Button todoButton; // Button is specified in the FXML file

    // Empty constructor (for dependency injection etc.)
    public TodoController() {
        this.backButton.setOnAction(event -> show("../"));
        this.todoButton.setOnAction(event -> show("/todo"));
    }
}
```

## 🧵 Sub-Controllers

Controllers can be used inside of other controllers to create reusable components. This can be done by using the
`@Providing` annotation on a field of type `Provider<T>` in your routing class, where `T` is the controller which should
be displayed.

In order to display a sub-controller, open the FXML file of the parent controller and add the sub controller as an
element like this:

```xml
<?import io.github.sekassel.jfxexample.controller.TodoController?>
...
<TodoController fx:id="yourid" onAction="onActionMethod" ... />
```

Depending on the parent you extended from, all attributes/properties available for this parent can be set for your
custom controller element as well.
