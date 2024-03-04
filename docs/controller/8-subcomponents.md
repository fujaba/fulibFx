# Subcomponents

Components can be used inside other controllers to reduce the complexity of a single controller by moving some features
into subcomponents (also called subcomponents).
The framework supports subcomponents in the form of components. Basic controllers are <u>not</u> supported as subcomponents.

Subcomponents can be added to a controller by creating a field of the required type and annotating it with `@SubComponent`.
The instance still has to be provided by the user, for example by using dependency injection.

```java
@Controller
public class TodoController {

    @FXML
    VBox container; // VBox is specified in the FXML file
    
    @SubComponent
    @Inject
    TodoListComponent todoListComponent;
    
    // ...
    
    @onRender
    public void onRender() {
        container.getChildren().add(todoListComponent); // Add the subcomponent to the view
    }
    
}
```

Components annotated with `@SubComponent` will be automatically initialized and rendered along with the parent controller.
Subcomponents can be used in the same way as normal controllers, meaning they can have their own subcomponents and
initialize/render methods.

### Subcomponents in FXML

Subcomponents can also be specified in FXML files. In order to display a subcomponent, open the FXML file of the
parent controller and add the subcomponent as an element like this:

```xml
<?import io.github.sekassel.jfxexample.controller.TodoController?>
...
<TodoController fx:id="yourid" onAction="onActionMethod" />
```

Depending on the parent you extended from, all attributes/properties available for this parent can be set for your
custom component element as well.

This will use one of the fields annotated with `@SubComponent` to display the subcomponent. The field has to be of the
same type as the component specified in the FXML file. Another way of providing the subcomponents to the FXML file 
is by creating a `Provider` of the subcomponent type and annotating it with `@SubComponent`. When looking for a suitable
subcomponent, the framework will first check the fields and then the providers.

If the `fx:id` attribute is specified, JavaFX will automatically inject the subcomponent instance back into the field.

If for example there are two subcomponents in the FXML file, the parent controller could have the following fields:

```xml
<DiceComponent/>
<DiceComponent/>
```

```java
@Subcomponent
@Inject // This provider will be used to create the subcomponents
Provider<DiceComponent> diceCompProvider;
```
```java
@Subcomponent
@Inject // This provider will be used to create the subcomponents
Provider<DiceComponent> diceCompProvider; 

@FXML // This field will be injected by JavaFX
DiceComponent dice1;

@FXML // This field will be injected by JavaFX
DiceComponent dice2;
```
```java
@Subcomponent
@Inject
@FXML // This field will be used as the instance for one subcomponent
DiceComponent dice1;

@Subcomponent
@Inject
@FXML // This field will be used as the instance for the other subcomponent
DiceComponent dice2;
```

### Managing subcomponents manually

There might be situations where you need a variable amount of subcomponents but constructs like a For-Loop is not suitable.
In this case you can also create or provide your own instance(s) and initialize/render it manually.

Either inject a `Provider<T>` and call `get()` or create/inject a new instance of the component manually.
After acquiring the instance, you can initialize and render it manually using the `initAndRender` method of the `FulibFxApp` class.
This method takes the component, a map of parameters and a container disposable (e.g. ComponentDisposable or Subscriber).
The method will return the rendered instance of the component. If a disposable has been provided, a cleanup task for this component will be added to it.
Otherwise, one has to manually clean up the component by calling the `destroy` method in the `FulibFxApp` class.

```java

@Controller
public class TodoController {

    @Inject
    App app; // App is the class extending FulibFxApp

    @FXML
    VBox container; // VBox is specified in the FXML file

    @Inject
    Provider<TodoListComponent> todoListComponentProvider;

    @Inject 
    @SubComponent // This component will be framework
    TodoManagerComponent todoManagerComponent;

    @Inject // Do not use @SubComponent annotation, as we want to manage the subcomponent manually
    TodoInputComponent todoInputComponent;

    @Inject
    Subscriber subscriber;
    
    // ... 

    @onRender
    public void onRender() {
        TodoListComponent result = app.initAndRender(todoListComponentProvider.get(), Map.of("param", value), subscriber); // This subcomponent has to be cleaned up manually by disposing the subscriber
        container.getChildren().add(result); // Add the subcomponent to the view
        
        // This subcomponent has to be cleaned up manually, e.g. by calling destroy in the app
        container.getChildren().add(app.initAndRender(todoInputComponent));

        // This subcomponent will be created based on the route and cleaned up by the subscriber
        TodoTaskBarComponent taskBar = app.initAndRender("/todo/taskbar", Map.of("param", value), subscriber); 
        
    }
}
```