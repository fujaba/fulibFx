# Routing

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

<img src="/.github/assets/route-diagram.png" height="300" alt="Routing tree showing main, login, todo and register routes in a tree like structure">

After setting up the router class, register it in the `FulibFxApp` class by calling the `registerRoutes(Object)` method.
It is recommended to use dependency injection (module/component) to provide a router instance to the method.

To display a controller, you have to call the `show()` method of the `FulibFxApp` class and pass the route (or the component instance).

```java
public class TodoApp extends FulibFxApp {
    
    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        // ...
        show(""); // Start in the Main Menu
    }
}
```