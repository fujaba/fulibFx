# ☕ fulibFx

fulibFx is a versatile framework for JavaFX applications that is specifically designed for MVC pattern projects.
It provides a simple way to create and manage controllers, views, routes, sub-controllers, modals, and more.

The framework is built on top of JavaFx and uses Dagger for dependency injection and RxJava for reactive programming.
It also provides a few utility classes and data structures to simplify the creation of JavaFX applications.

## ❓ How to start?

To make use of this framework, you need to create an `App` class (e.g., `TodoApp.class`) that extends the `FulibFxApp`
class and overrides its `start` method. If you already have a JavaFX App class, you can easily migrate by
changing `extends Application` to `extends FulibFxApp`.

In the `start` method, you should call `super.start(primaryStage)` to initialize the framework. After that, you can
proceed to set up your routes and controllers.

```java
public class TodoApp extends FulibFxApp {

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

The app can be started as usual, for example by pressing the run button in your IDE or by running the `main` method.
If you want to start in development mode, you can set the `INDEV` environment variable to `true` before starting the app.

## 🎮 Controllers

Controllers are the backbone of your application. To set up a controller, create a class where the controller logic will
be defined and annotate it with `@Controller`.

```java

@Controller
public class TodoController {

    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
    }
    
}
```

### ✨ Initialization, rendering and cleanup

Within your controller class, you have the ability to define methods that are automatically triggered when the
controller is initialized, rendered or destroyed. These methods should be annotated with either `@onInit`, `@onRender`
or `@onDestroy` to specify their respective execution points.

```java

@Controller
public class TodoController {

    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
    }

    @onInit
    public void thisMethodWillBeCalledOnInit() {
        // Called when the controller is initialized
        System.out.println("Controller initialized");
    }

    @onRender
    public void thisMethodWillBeCalledOnRender() {
        // Called when the controller has been loaded and is ready to be displayed
        System.out.println("Controller rendered");
    }

    @onDestroy
    public void thisMethodWillBeCalledOnDestroy() {
        // Called when the controller is being cleaned up
        System.out.println("Controller destroyed");
    }
    
}
```

The initialization of a controller takes place when the controller is created, just before it is fully loaded. During
this phase, you may not have access to elements defined in the corresponding view.

On the other hand, the rendering of a controller occurs when the controller is fully loaded and ready to be displayed.
At this stage, you have full access to all elements defined in the corresponding view.

The destruction of a controller takes place when the controller is no longer needed. This can happen when a new controller 
is displayed using the `show()` method or when the application is closed. During this phase, you should clean up any 
resources that are no longer needed.

### 🎫 Parameters

To pass parameters to a controller, you can provide an additional argument to the show method, consisting of a map of
strings and objects. The strings specify the argument's name and the objects are the value of the argument. For example,
`show("/route/to/controller", Map.of("key", value, "key2", value2))` will pass the value `value` to the argument `key`.

To use a passed argument in a field or method, you have to annotate it with `@Param("key")`. The name of the parameter
will be used to match it to the map of parameters passed to the `show()` method. If the annotation is used on a field,
the field will be injected with the value of the parameter before the controller is initialized. If the annotation is used
on a method, the method will be called with the value of the parameter before the controller is initialized. If the
annotation is used on a method parameter of a render/init method, the method will be called with the value of the parameter.

If `@Param` is used on a field containing a `WriteableValue` (e.g. a `StringProperty`), the value will be set to the parameter.

Instead of accessing the parameters one by one, you can also use the `@ParamsMap` annotation to inject a map of all parameters.
This annotation can be used for fields and method parameters of type `Map<String, Object>`. If the annotated field is final,
`putAll` will be called instead.

If you want to call a setter method with multiple parameters, you can use the `@Params` annotation to specify the names of
the parameters that should be passed to the method. This annotation can be used for methods with multiple parameters.
The order of the parameters in the method has to match the order of the names in the annotation.

In order to pass arguments to the following controller, the method `show("/route/to/controller", Map.of("fofo", myFoo, "baba", myBa))`
would have to be called.

```java

@Controller
public class FooController {
    
    // The parameter 'baba' will be injected into this field before the controller is initialized
    @Param("baba")
    private Bar bar;
    
    // This field will be injected with a map of all parameters before the controller is initialized
    @ParamsMap
    private Map<String, Object> params;
    
    // Default constructor (for dependency injection etc.)
    @Inject
    public FooController() {
    }
    
    @Params({"fofo", "baba"}) // This also works with @Param and @ParamsMap
    public void setFoo(Foo foo, Bar bar) {
        // This method will be called with the parameter 'fofo' and 'baba' before the controller is initialized
    }

    @onRender
    public void render(@Param("fofo") Foo foo, @ParamsMap Map<String, Object> params) {
        // This method will be called with the parameter 'fofo' and a map of all parameters upon rendering
    }
    
}

```

If a controller expects an argument but no argument with a suitable name is passed, `null` will be passed instead.
Any arguments not expected by the controller will be ignored. 

If an argument is provided, but the type doesn't match the type of the field or method parameter, an exception will be 
thrown.

The order of injection is as follows:
1. Fields will be injected with `@Param` annotations and `@ParamsMap` annotations
2. Methods annotated with `@Param` will be called
3. Methods annotated with `@Params` will be called
4. Methods annotated with `@ParamsMap` will be called
5. The controller will be initialized (`@onInit`)
6. The controller will be rendered (`@onRender`)

## 💭 Components

Components are a special type of controller that can be used to create reusable components. Components have to extend 
from a JavaFX Parent (or any class extending from Parent) and have to be annotated with `@Component`. 

```java

@Component
public class MyComponent extends VBox {
}
```

Components can be used in the same way as controllers, but they can also be used as sub-controllers.
As components are much more versatile than controllers, they are the recommended way for creating your application.

## 📷 Views

Each controller is associated with a view, which is composed of one or more nested JavaFX elements (panes, buttons,
labels, etc.). You have four different options to define the view of a controller:

### 📎 Using FXML to define the view

Create an FXML file with a name based on the controller class (e.g., `TodoController` --> `todo.fxml`) and place it in
the 'resources' directory at the same path as your app class.

You can also define a custom FXML path to load instead by setting the `view` path in the `@Controller` annotation. Note
that the entered path will always be relative to the path of your app class in the `resources/` directory.

```java

// Leaving 'view' blank will use the default file name (e.g. TodoController --> todo.fxml)
@Controller(view = "view/todo.fxml")
public class TodoController {

    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
    }
    
}
```

When displaying this controller, the framework will automatically load the corresponding FXML file and set it as the
view.

This method is only viable for displaying main controllers as sub controllers have to be a JavaFX element. Therefore, it
is recommended to use one of the following methods.

### ⚙ Using a method to define the view

If, for some reason, you need special loading logic, you can also define the view by creating a method in the controller
class that returns a JavaFX parent element (e.g., `Pane`, `Button`, `Label`, etc.). This method will be called when the
view is loaded.

You can define the method you want to use by setting the method in the `@Controller` annotation, starting with a '`#`'.

```java

@Controller(view = "#renderThis")
public class TodoController {

    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
    }

    public VBox renderThis() {
        return new VBox(new Label("Hello World"));
    }
}
```

### ☁ Using the component as the view

As components extend from a JavaFX Parent (or any class extending from Parent), the view can be set to the
element the component represents. You can use this to create simple views without the need for an FXML file. More
complex views should not be created this way. Instead, you should use the `fx:root` tag (see below).

```java

@Component
public class TodoController extends VBox {

    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
        this.getChildren().add(new Label("Hello World"));
    }
    
}
```

When displaying this component, the framework will automatically set the view to the VBox element, including all the
modifications like added children, etc.

### 💾 Using JavaFX Root elements (recommended)

The previously mentioned methods can be combined by using
the [`fx:root` tag](https://openjfx.io/javadoc/20/javafx.fxml/javafx/fxml/doc-files/introduction_to_fxml.html#root_elements)
in the FXML file. This way you can use the FXML file to define a more complex view and the JavaFX element to add
additional functionality. This will also be very helpful when using the component as a sub-controller.

```java

@Component(view = "view/todo.fxml")
public class TodoController extends VBox {

    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
    }
}
```

```xml

<fx:root type="VBox" fx:controller="io.github.sekassel.todo.TodoController">
    <Label text="TODO"/>
    <Label fx:id="todoLabel"/>
    <Button fx:id="deleteButton" mnemonicParsing="false" text="Remove"/>
</fx:root>
```

When displaying this controller, the framework will automatically load the corresponding FXML file and set your
controller as the controller of the root element. The root element will then be set as the view of the controller.

## 🧵 Sub-Controllers

Components can be used inside other controllers to reduce the complexity of a single controller by moving some features
into sub-controllers (also called subcomponents).
The framework supports sub-controllers in the form of components. Basic controllers are <u>not</u> supported as sub-controllers.

Sub-controllers can be added to a controller by creating a field of the required type and annotating it with `@SubComponent`.
The instance still has to be provided by the user, for example by using dependency injection.

```java
@Controller
public class TodoController {

    @FXML
    VBox container; // VBox is specified in the FXML file
    
    @SubComponent
    @Inject
    TodoListComponent todoListComponent;
    
    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
    }
    
    @onRender
    public void onRender() {
        container.getChildren().add(todoListComponent); // Add the sub-controller to the view
    }
    
}
```

Components annotated with `@SubComponent` will be automatically initialized and rendered along with the parent controller.
Sub-controllers can be used in the same way as normal controllers, meaning they can have their own sub-controllers and
initialize/render methods.

### 📧 Sub-Controllers in FXML

Sub-controllers can also be specified in FXML files. In order to display a sub-controller, open the FXML file of the
parent controller and add the sub-controller as an element like this:

```xml
<?import io.github.sekassel.jfxexample.controller.TodoController?>
        ...
<TodoController fx:id="yourid" onAction="onActionMethod" ... />
```

This will use one of the fields annotated with `@SubComponent` to display the sub-controller. The field has to be of the
same type as the component specified in the FXML file. If the `fx:id` attribute is specified, JavaFX will automatically
inject the controller into the field.

Depending on the parent you extended from, all attributes/properties available for this parent can be set for your
custom controller element as well.

### ✋ Managing sub-controllers manually

There might be situations where you need a variable amount of sub-controllers but a For-Loop is not suitable.
In this case you can also create or provide your own instance(s) and initialize/render it manually.

Either inject a `Provider<T>` and call `get()` or create/inject a new instance of the controller manually.
After aquireing the instance, you can initialize and render it manually using the `initAndRender` method of the `FulibFxApp` class.
This method takes the controller, a map of parameters and a container disposable (e.g. ComponentDisposable or Subscriber).
The method will return the rendered instance of the component. If a disposable has been provided, a cleanup task for this component will be added to it.
Otherwise, one has to manually cleanup the component by calling the `destroy` method in the `FulibFxApp` class.

```java

import java.util.Map;

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

    @Inject // Do not use @SubComponent annotation, as we want to manage the sub-controller manually
    TodoInputComponent todoInputComponent;

    @Inject
    Subscriber subscriber;

    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
    }

    @onRender
    public void onRender() {
        TodoListComponent result = app.initAndRender(todoListComponentProvider.get(), Map.of("param", value), subscriber); // This sub-controller has to be cleaned up manually by disposing the subscriber
        container.getChildren().add(result); // Add the sub-controller to the view
        
        // This sub-controller has to be cleaned up manually, e.g. by calling destroy in the app
        container.getChildren().add(app.initAndRender(todoInputComponent));
        
    }
}
```

## 🚮 Destroying controllers

When a controller is no longer needed, it should be destroyed to free up resources. This will automatically happen when
a new controller is shown using the `show()` method. However, if you for example subscribe to observables, the framework
will not clear them up them automatically. You should therefore save the disposables of your subscriptions and dispose them
when the controller is destroyed.

This can be done by creating a `CompositeDisposable`, adding all disposables to it and then calling `compositeDisposable.dispose()`
in a `@onDestroy` annotated method.

The framework also provides utility classes for dealing with subscriptions and other mechanisms requiring cleanup.
By creating a new `Subscriber` instance (or by using dependency injection to provide one) and using its utility methods,
you can easily manage subscriptions without having to worry about disposing them one by one. Using `subscriber.dispose()`
will dispose all subscriptions added to the subscriber. When running in dev mode, destroying a controller will check if
all subscriber fields have been disposed and will print a warning if not.

```java

@Controller
public class TodoController {

    @Inject
    Subscriber subscriber;
    
    @Inject
    TodoService todoService;

    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
    }

    @onRender
    public void onRender() {
        this.subscriber.subscribe(this.todoService.getTodos(), todos -> {
            // Do something with the todos
        }); 
        this.subscriber.subscribe(() -> {
            // Add custom logic to be executed when the controller is destroyed
        });
    }
    
    @onDestroy
    public void onDestroy() {
        this.subscriber.dispose();
    }
    
}
```

## 🌍 Routes

Routes are the main way to navigate between views. To set up routes to different views, you have to create a class where the
routes will be defined.

Inside the class, you have to create a field for each route you want to define. The field has to be annotated with
`@Route("/your/route")` and has to be of type `Provider<T>`, where `T` is the controller which should be displayed at
the
route.

If the path of the route isn't specified, the name of the field will be used as the route name.

The example below uses Dagger to inject the controllers into the routing class. If you don't want to use dependency
injection, you can also create the providers manually.

```java

public class Routing {

    @Inject
    @Route("")
    public Provider<MainMenuController> main;

    @Inject
    @Route("/login")
    public Provider<LoginController> login;

    @Inject
    @Route("/login/register")
    public Provider<RegisterController> register;

    @Inject
    @Route // Route name will be '/todo'
    public Provider<TodoController> todo;

    @Inject
    public Routing() {
        // Default constructor (for dependency injection etc.)
    }

}
```

This setup will result in the following routing tree:

<img src=".github/assets/route-diagram.png" height="300" alt="Routing tree showing main, login, todo and register routes in a tree like structure">

After setting up the router class, register it in the `FulibFxApp` class by calling the `registerRoutes(Object)` method.
It is recommended to use dependency injection (module/component) to provide a router instance to the method.

## 🖥 Displaying controllers

To display a controller, you have to call the `show()` method of the `FulibFxApp` class and pass the route (or the component instance).

```java
public class TodoApp extends FulibFxApp {
    
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
also supports path traversal (e.g., '`../login`'). This can be used to create a back button.

```java

@Controller
public class TodoController {

    @FXML
    Button backButton; // Button is specified in the FXML file

    @FXML
    Button todoButton; // Button is specified in the FXML file

    // Default constructor (for dependency injection etc.)
    @Inject
    public TodoController() {
    }

    @onRender
    public void addButtonAction() {
        this.backButton.setOnAction(event -> show("../"));
        this.todoButton.setOnAction(event -> show("/todo"));
    }
    
}
```

The `show()` method internally calls the `display(Parent parent)` method, which could be overridden to provide additional 
functionality like having multiple screens on top of each other (e.g. a sidebar or toolbar) or to add custom display logic. 

If you just want to listen to a controller being displayed and don't want to change the display logic, you can override 
the `onShow()` method instead.

## ⌚ History and refresh

The framework also provides a history of visited routes. The history acts like a stack and can be
used to go back and forth between previously visited routes. The history is automatically updated when using the `show` 
method. The history works like the history of a browser, meaning you can go back and forth again, but after going back
and visiting an alternative route, the routes that were previously in the history will be removed.

The history can be navigated using the `back()` and `forward()` methods of the `FulibFxApp` class.

Using the `refresh()` method of the `FulibFxApp` class, you can refresh the currently displayed controller. This will
destroy the controller and reload it with the same parameters as before. This can be used to update the view of a
controller whilst being in dev mode. Refreshing a controller will run the `onDestroy` method of the controller and then 
run the `onInit` and `onRender` methods again.

When being in dev mode, the framework will automatically refresh the controller when the corresponding FXML file is
changed. This can be used to quickly test changes to the view without having to restart the application.

## 🔁 For-Loops

For-Loops can be used to easily display a node/sub-controller for all items in a list. Whenever an item is added to or
removed from the list, the list of nodes updates accordingly.

The easiest form of a For-Loop can be achieved like this:

```java
For.of(container, items, myComponentProvider);
```

This will create a component for each item in the list `items` and add it to the children of the `container` (e.g. a VBox).

Currently, no information is passed to the created label. In order to pass static information you can add
parameters like you would when using the `show`-method using a map.

```java
For.of(container, items, myComponentProvider, Map.of("key", value));
For.of(container, items, myComponentProvider, params); // Parameters can be taken from the @Params annotation for example
```

If you want to pass dynamic information like binding the item to its controller, you can use an `BiConsumer`.
The `BiConsumer` allows to define actions for initializing each controller based on its item.

```java
For.of(container, items, myComponentProvider, (controller, item) -> {
    controller.setItem(item);
    controller.foo();
    controller.bar();
});

For.of(container, items, myComponentProvider, ExampleComponent::setItem); // Short form using method references
For.of(container, items, myComponentProvider, Map.of("key", value), ExampleComponent::setItem); // Static and dynamic information can be passed together
```

Instead of a controller you can also define a basic JavaFX node to display for every item.

```java
For.of(container, items, () -> new Button("This is a button!"));
For.of(container, items, () -> new VBox(new Button("This is a button!"))); // Nodes can have children
```

Unlike with controllers, it is not possible to pass static information in the form of paramters to nodes, as there is no
way of accessing them in the code. However, dynamic information in the form of an `BiConsumer` can be used just like with 
controllers.

```java
For.of(container, items, () -> new Button(), (button, item) -> {
    button.setText("Delete " + item.name();
    button.setOnAction(event -> items.remove(item));
});
```

In order to destroy controllers generated by the For-loops, you can use the `dispose()` method of the `For` class or add
the return value of the `disposable()` method to your list of disposables.

## Ⓜ Modals

Modals are a special type of controller that can be used to display a controller on top of another controller. Modals
can be used to display popup windows, dialogs, etc.

The framework provides a `Modals` class that can be used to display a modal.

```java
Modals.showModal(app.stage(), confirmComponent, (stage, scene, component) -> {
    stage.setTitle("Modal");
    component.setConfirmAction(() -> {
        // Do something
        stage.close();
    });
});

```

## 👯‍♂️ Duplicate nodes

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

## ↘ Call order

Since the framework differentiates between initialization and rendering, different methods annotated
with `@onInit` or `@onRender` are called at different times.

The framework has a general rule of **initialization before rendering**, meaning you cannot access most JavaFX
elements (for example nodes defined in an FXML file) in the init methods as the elements aren't loaded before the
rendering.

When using sub-controllers or For-Loops, the order of operations is a bit more complex. At first the main controller
will be initialized. After the controller has been initialized, all sub-controllers will be loaded and therefore
initialized. This will happen recursively until a sub-controller doesn't have any sub-controllers. After that, the
sub-controllers will be rendered, going back up to the main controller. The main controller will be rendered, after all
the sub-controllers have been rendered.

If a For-Loop is defined in a method annotated with `onRender` in any (sub-)controller,
the `onRender` will (obviously) be called first. After that, the initializer (`BiConsumer`) of the 
for-Controller will be called and then the for-controller will be initialized and then rendered.

#### Example

```java

@Controller("example.fxml")
public class ExampleController {

    // Constructor, elements etc.
    // This controller has a sub-controller defined in the FXML file

    @onInit
    public void onInit() {
        System.out.println("onInit Controller");
    }

    @onRender
    public void onRender() {
        System.out.println("onRender Controller");
        For.controller(container, items, ForController.class, (controller, item) -> System.out.println("Initializer ForController"));
    }

}
```

```java

@Component("sub.fxml")
public class SubController extends VBox {

    // Constructor, elements etc.
    // This controller has another sub-controller defined in the FXML file

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

@Component()
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
Constructor ForController
Initializer ForController
onRender ExampleController
onInit ForController
onRender ForController
```

## 📦 Other Data structures
The framework provides a few data structures that can be used to simplify the creation of JavaFX applications.

### Duplicator
A Duplicator is a functional interface that takes an object and returns a duplicate of the object. It can be used to
create copies of Objects such as JavaFX nodes. The framework used Duplicators for the For-Loops.
For more information, see the section about [Node duplication](#-duplicate-nodes).

### Subscriber
The subscriber is a utility class that can be used to manage subscriptions. It can be used to subscribe to observables
without having to worry about disposing them one by one. For more information, see the section about [cleanup](#-destroying-controllers).

### RefreshableDisposable
The RefreshableDisposable is an interface defining disposables that can be refreshed. Refreshing a disposable will
make it usable again, after it has been disposed. An example for this is the `RefreshableCompositeDisposable`.

### ItemListDisposable
The ItemListDisposable will run an action for all added items upon disposal. This can be used to clean up items in a list
with a certain action in a single disposable.

```java
ItemListDisposable<String> disposable = ItemListDisposable.of(item -> print(item), "!", "World");
disposable.add("Hello");
disposable.dispose(); // Will print "Hello", "World" and "!" to the console (LIFO order)
```

## TraversableQueue
A TraversableQueue is a queue that saves the latest n entries made to it. Like a normal queue, it can be used to store items in a FIFO order.
When the queue is full, the oldest item will be removed. However, it also provides methods to traverse the queue,
meaning you can go back and forth between items in the queue. When you go back in the queue and add a new item, all
items after the current item will be removed. This can be compared to the history of a browser and is used for the
history of routes.

## 🛑 Common issues

### 1. My route is not found even though it is registered
When using `show("route/to/controller")` without a leading "`/`", the route is relative to the currently displayed controller. 
Meaning if you are currently displaying the controller `"/foo/bar"` and you call `show("baz")`, the route will be `"/foo/bar/baz"`.
If you want to display a controller from the root, you have to start the route with a "`/`".

### 2. Weird things happen during refresh
When refreshing a controller, the controller is destroyed and then reloaded with the same parameters as before. 
If an object has been passed as a parameter and the object has been modified during the lifetime of the controller,
the already modified object will be passed after the refresh, just to be modified again. This can lead to unexpected 
behaviour. To avoid this, you should try to not modify objects passed as parameters. Instead, you should create a copy 
of the object and modify the copy or modify the object before passing it to the controller.