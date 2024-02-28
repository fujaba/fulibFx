# Titles

The title of a controller or component can be set by annotating The class with `@Title`.
The annotation takes an argument specifying the title of the controller or component.
If no title is specified in the annotation, the transformed name of the class will be used as the title.

```java
@Controller
@Title("My Todo List")
public class TodoController {
    // ...
}
```

If the controller or component specifies a resource bundle, the title can be a key in the resource bundle (e.g. `@Title("%title.key")`).
The framework will then automatically set the title of the controller/component to the value of the key in the resource bundle.
See the section about [internationalization](5-internationalization.md) for more information.

```java
@Controller
@Title("%title.todo")
public class TodoController {
    
    @Resource
    ResourceBundle resourceBundle;
}
```

When displaying this controller/component, the framework will automatically set the title of the window to "My Todo List".
In order to eliminate redundancy, you can use the `setTitlePattern` method of the `FulibFxApp` class to set a pattern
which will be used to format the title of the window. The pattern can either be provided as a string containing a placeholder
for the title or as a function taking the title as an argument and returning the formatted title.

```java
   
@Override
public void start(Stage primaryStage) {
    super.start(primaryStage);
    setTitlePattern("TODO - %s"); // Results in "TODO - My Todo List"
    setTitlePattern(title -> "TODO - " + title + " v1.0"); // Results in "TODO - My Todo List v1.0"
}
```