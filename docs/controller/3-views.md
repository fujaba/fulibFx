# Views

Each controller is associated with a view, which is composed of one or more nested JavaFX elements (panes, buttons,
labels, etc.). The framework provides several ways to define the view of a controller.

The views can be display by calling the `show()` method of the `FulibFxApp` class and passing the route (or the
controller instance).

### FXML files

Create an FXML file with a name based on the controller class (e.g., `TodoController` --> `todo.fxml`) and place it in
the 'resources' directory at the same path as the controller class.

You can also define a custom FXML path to load instead by setting the `view` path in the `@Controller` annotation. Note
that the entered path will always be relative to the path of your controller class in the `resources` directory.

```java

// Leaving 'view' blank will use the default file name (e.g. TodoController --> Todo.fxml)
@Controller(view = "view/Todo.fxml")
public class TodoController {
    // ...
}
```

When displaying this controller, the framework will automatically load the corresponding FXML file and set it as the
view.

This method is only viable for displaying main controllers as sub controllers have to be a JavaFX element. Therefore, it
is recommended to use one of the following methods.

### View methods

If, for some reason, you need special loading logic, you can also define the view by creating a method in the controller
class that returns a JavaFX parent element (e.g., `Pane`, `Button`, `Label`, etc.). This method will be called when the
view is loaded.

You can define the method you want to use by setting the method in the `@Controller` annotation, starting with a '`#`'.

```java

@Controller(view = "#renderThis")
public class TodoController {
    
    // ...
    
    public VBox renderThis() {
        return new VBox(new Label("Hello World"));
    }
}
```

### Component views

As components extend from a JavaFX Parent (or any class extending from Parent), the view can be set to the
element the component represents. This can be used to create simple views without the need for an FXML file. More
complex views should not be created this way. Instead, the `fx:root` tag should be used (see below).

```java

@Component
public class TodoController extends VBox {

    public TodoController() {
        this.getChildren().add(new Label("Hello World"));
    }
    
    // ...
    
}
```

When displaying this component, the framework will automatically set the view to the VBox element, including all the
modifications like added children, etc.

### Root elements in FXML files

The previously mentioned methods can be combined by using the 
[`fx:root` tag](https://openjfx.io/javadoc/20/javafx.fxml/javafx/fxml/doc-files/introduction_to_fxml.html#root_elements)
in an FXML file. Using a root element eliminates the tedious work of having to manually add all child elements whilst
still being able to directly work with the element inside the class. This will also be very helpful when using the 
component as a subcomponent in another controller.

```java

@Component(view = "view/Todo.fxml")
public class TodoController extends VBox {
    // ...
}
```

```xml

<fx:root type="VBox" fx:controller="de.uniks.todo.TodoController">
    <Label text="TODO"/>
    <Label fx:id="todoLabel"/>
    <Button fx:id="deleteButton" mnemonicParsing="false" text="Remove"/>
</fx:root>
```

When displaying this controller, the framework will automatically load the corresponding FXML file and set your
component as the controller of the root element. The root element will then be set as the view of the controller.

---

[⬅ Components](2-components.md) | [Overview](README.md) | [Parameters ➡](4-parameters.md)