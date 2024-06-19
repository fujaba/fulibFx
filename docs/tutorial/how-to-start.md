## How to start?

In the following section you will find a step-by-step guide on how to start a new project using `FulibFx`.
The guide will cover the entire process of re-creating the base for the board game Ludo.
Some parts such as Dagger and RxJava will only be covered briefly, as they are not the main focus of this tutorial.
The game logic itself also plays a minor role in this tutorial, as the code can be looked up in the [ludo example](../../ludo/src/main/java).

This tutorial is based on gradle and IntelliJ IDEA, but you can use any other IDE or build tool.

### Prerequisites/Dependencies

To start a new project, set up a new workspace directory in your IDE and add the required dependencies.
Assuming you are already installed [JavaFX](https://openjfx.io/openjfx-docs/#gradle),
[Dagger](https://github.com/google/dagger?tab=readme-ov-file#gradle) and
[RxJava](https://github.com/ReactiveX/RxJava?tab=readme-ov-file#setting-up-the-dependency), you can add the following
dependencies to your `build.gradle` file:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.fulib:fulibFx:VERSION'
    implementation 'org.fulib:fulibFx-processor:VERSION'
}
```

After adding the dependencies, refresh your project and you are ready to start.

### Create a new project

Like with JavaFX, the first step of starting a new project is to create a new class which will be the starting point of
your application. Instead of extending `Application`, we will extend `FulibFxApp` and override the `start` method.

Make sure to call `super.start(primaryStage)` to initialize the framework.
Otherwise, the application will not work as expected.

During the `start` method, we will later configure things like routing and show the first view.
For now, we can add some basic configuration such as configuring the resource path and setting up the auto refresher.

```java
public class App extends FulibFxApp {

    @Override
    public void start(Stage primaryStage) {
        try {

            // Starting the framework, initializes all the necessary components
            super.start(primaryStage);

            // Setting the resource path to the resources folder of the project (required for reloading in dev)
            // If the resource path is not set, the framework will use the default resource path (src/main/resources)
            setResourcesPath(Path.of("ludo/src/main/resources/"));

            // Setting the path which the auto refresher should watch (required for auto-reloading in dev)
            autoRefresher().setup(Path.of("ludo/src/main/resources/de/uniks/ludo"));

            // ...
        } catch (Exception e) {
            // If an error occurs while starting the application, we want to log it
            LOGGER.log(Level.SEVERE, "An error occurred while starting the application: " + e.getMessage(), e);
        }
    }
}
```

When using Dagger, add the following snippet module to provide the `FulibFxApp` to internal components using your app class.

```java
@Module
public class MainModule {

    // ...

    @Provides
    FulibFxApp app(LudoApp app) {
        return app;
    }
}
```

The app can now be started by creating a main method and calling the `launch` method of the `Application` class.

```java
public static void main(String[] args) {
    Application.launch(App.class, args);
}
```

Right now, nothing will happen when you run the application, as we have not yet added any content.
In the next section, we will create a simple controller with a view and add it to the application.

### Controllers and Views

In `FulibFx`, the application is built using controllers and views.
A controller describes the behavior of the application and links the model with the view, which defines the look of the
frontend.

In order to set up a basic controller, create a new class and annotate it with `@Controller`.
This will mark the class as a controller for later usage in the framework.
In this example, we will create a controller for configuring the amount of players for our ludo game.

```java

@Controller
public class SetupController {

    @Inject
    public SetupController() {
        // Required for creating instances using Dagger
        // See https://dagger.dev/tutorial/
    }

}
```

Right now, our controller doesn't do much and if we try to display its view, the application would crash as we haven't
configured anything yet.

In order to set the controller's view, the `@Controller` annotation can be configured with a path specifying an FXML
file.
The path is always **relative to the package of the class**.

```java
// Path would be "src/main/resources/de/uniks/controller/Setup.fxml" for example
@Controller(view = "Setup.fxml")
public class SetupController {
    // ...
}
```

Often times the name of the controller class and the name of the FXML will match each other.
In order to remove the need of having to retype the basically same name twice, a default value will be used if no path is provided.
This will be the name of your class with `Controller` or `Component` removed.
The class name `MySetupController` would be transformed to `MySetup.fxml`.

As the names would be the same in our example, we will remove it again for simplicity's sake.

If the controller were to be displayed now, the FXML file specified using the annotation would be used to describe its
view. If the `fx:controller` attribute is set in the FXML file, the controller will be automatically linked to the view
so that `@FXML` annotated fields can be used to access the view's elements.

In our example the view will contain a label, a slider and a button to start the game.
The button is linked with the `onPlayClicked` method, which will be called when the button is clicked.

```java

@Controller
public class SetupController {

    @FXML
    private Slider playerAmountSlider;

    @Inject // Using dagger to inject the app
    App app;

    @Inject
    public SetupController() {
    }

    @FXML
    public void onPlayClick() {
        int playerAmount = (int) this.playerAmountSlider.getValue();
        // TODO: Start the game with the given amount of players
    }
}
```

All other controllers can be created in the same way.

### Routing

In order to display the view of the controller, we need to add it to the application's routing.
Routes can be configured by creating a new class containing `Provider<?>` fields for each controller.

```java
public class Routing {

    @Inject
    @Route("")
    // The empty route is the default route. It is often used as the starting point of the application.
    public Provider<SetupController> setupController;

    @Inject
    @Route("ingame")
    // Routes can be used to show controllers. Using the route "/ingame" will show the IngameController.
    public Provider<IngameController> ingameController;

    @Inject
    @Route("ingame/gameover")
    public Provider<GameOverController> gameOverController;

    // ...
}
```

The `@Route` annotation is used to specify the route of the controller.
The provider is used to create a new instance of the controller when the route is accessed.

In order to register the routing, the `Routing` class needs to be set using the `registerRoutes(Object router)` method
in
the `FulibFxApp` class. To display the view of the controller, the `show` method can be used. This method takes the
route of the controller and an optional `Map<String, Object>` containing parameters for the controller.

```java
public class App extends FulibFxApp {


    @Override
    public void start(Stage primaryStage) {
        try {
            super.start(primaryStage);
            // ...
            Routing routing = ...; // Create an instance, for example using Dagger
            registerRoutes(routing);
            show(""); // Show the setup view
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while starting the application: " + e.getMessage(), e);
        }
    }
}
```

Using the `show` method, the view of the `SetupController` will be displayed when the application is started.
Now we can start the application and see the view of the `SetupController`.

### Initialization, Rendering and Destruction

The lifecycle of a controller is divided into three phases: initialization, rendering and destruction.
Between rendering and destruction, the controller is in an active state and can be used to interact with the view.

The initialization phase is used to set up the controller and configure some initial values.
This phase is called after the controller is created and before the view is rendered.

The rendering phase is used to display the view of the controller.
This phase is called after the initialization phase when the controller view has been created and is ready to be
displayed.

The destruction phase is used to clean up the controller and remove any references to the view.
This phase is called after the view has been removed and the controller is no longer needed.

In order to add custom behavior to the lifecycle of a controller, the `@OnInit`, `@OnRender` and `@OnDestroy`
annotations
can be used to mark methods which should be called during the respective phase.
If these methods have to be executed in a specific order, an additional integer parameter can be used to specify the
execution order.

The following method will be called after the controller's view has been loaded and is ready to be displayed.

```java

@OnRender
public void drawBoard() {
    for (Field field : this.game.getBoard().getFields()) {
        Circle circle = createFieldCircle(field);
        this.boardPane.getChildren().add(circle);
        this.fieldToCircle.put(field, circle);
    }
}
```

These methods can be used to initialize the controller and configure its view, but for more complex behavior where we
have to
pass information to the controller, the `@Param` annotation can be used.

### Parameters

In order to start the game, we have to somehow pass the amount of players to the `IngameController`.
This can be done by adding parameters to the route when showing the controller.

```java
public class SetupController {
    // ...
    @FXML
    public void onPlayClick() {
        int playerAmount = (int) this.playerAmountSlider.getValue();
        app.show("ingame", Map.of("playerAmount", playerAmount));
    }
}
```

These parameters can be injected into the controller using the `@Param` annotation.

```java
@Controller
public class IngameController {
    // ...

    @OnInit
    public void setup(@Param("playerAmount") int playerAmount) {
        if (this.game == null) this.game = this.gameService.createGame(playerAmount);
        this.currentPlayer.set(this.game.getCurrentPlayer());
    }
}
```

In this example, when using `show("ingame", Map.of("playerAmount", playerAmount))`, the `playerAmount` parameter will be
injected into the `setup` method of the `IngameController` when the controller is initialized.

Besides from event methods, the annotation can also be used to mark fields and setter methods.
For more information, see the [documentation](../../README.md#-parameters).

### Resources

JavaFX supports the usage of language files to provide translations for the application.
In order to use language files in `FulibFx`, the `@Resource` annotation can be used to mark fields which should be
used as the resource bundle for the controller the field is in.

```java
@Controller
public class IngameController {

    @Resource
    ResourceBundle bundle;

    // ...
}
```

When loading the controller's view, the marked resource bundle will be used to provide translations for the keys specified
in the FXML file.

It can also be used to translate titles into different languages.

### Titles

In order to set the title of the application, the `@Title` annotation can be used on the controller class.
This will set the title of the application to the value of the annotation.

```java
@Title("Set up the game")
@Controller
public class SetupController {
    // ...
}
```

Since we already set up a resource bundle for the `IngameController`, we can use it to translate the title into different
languages by using a key instead of a fixed value.

```java
@Title("%ingame.title")
@Controller
public class IngameController {
    // ...
}
```

In order to specify what exactly our titles should look like when applied to the application, we can change the format
of the title using the `setTitlePattern` method in the `FulibFxApp` class.

```java
public class App extends FulibFxApp {

    @Override
    public void start(Stage primaryStage) {
        try {
            super.start(primaryStage);
            // ...
            setTitlePattern("Ludo - %s");
            setTitlePattern(title -> "Ludo - " + title); // Same as above
            // ...
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while starting the application: " + e.getMessage(), e);
        }
    }
}
```

### Subscriber

Often times you have to link object's properties together or listen to changes in a property.
JavaFX and RxJava offer many convenient ways to do this, but all of them have to be cleaned up manually.

In `FulibFx`, a `Subscriber` can be used to listen to changes in a property and automatically clean up the subscription
when the controller is destroyed.
Instead of having to dispose every subscription manually, the `Subscriber` will take care of it for you when calling its
`dispose` method in the `@OnDestroy` phase.
The Subscriber contains many different methods to listen to changes in a property, bind properties together, subscribe to
an observable and more.

```java
@Controller
public class IngameController {

    @Inject // Injected by Dagger for convenience
    Subscriber subscriber;

    @OnRender
    public void setupTexts() {
        this.subscriber.listen(
                game.listeners(),
                Game.PROPERTY_CURRENT_PLAYER,
                evt -> this.currentPlayer.set((Player) evt.getNewValue())
        );
        this.subscriber.bind(this.playerLabel.textProperty(), this.currentPlayer.map(Player::getId).map(id -> this.bundle.getString("ingame.current.player").formatted(id)));
    }

    @OnDestroy
    public void destroy() {
        // ...
        this.subscriber.dispose();
    }
    
    // ...

}
```

### Subcomponents

Components are a special type of controller which can be used inside other controllers to split the application into
smaller parts.

In order to create a component, the `@Component` annotation can be used to mark the class as a component.
Every component also is a controller and can be used in the same way as a controller.

```java
@Component(view = "Dice.fxml")
public class DiceSubComponent extends VBox {
    
    @FXML
    public Label eyesLabel;
    
    @OnRender
    public void render() {
        // ...
    }
}
```

The main difference between creating a component and a controller is that a component is a subclass of a JavaFX parent.
This means that a component can be used as a JavaFX node and can be added to the view of another controller.

Setting the view works slightly different from the view of a controller as well.
As you can see in the example, the FXML file is specified even though the name of the component and the FXML file match.
This is because if you leave the `view` attribute empty, the framework will use the class itself as the view.
If the FXML file is specified, the framework will load the FXML file and set the class object as the root node of the FXML
file. This means that the FXML file has to contain an `fx:root` [element](https://openjfx.io/javadoc/21/javafx.fxml/javafx/fxml/doc-files/introduction_to_fxml.html#root_elements).

```xml

<fx:root  type="VBox" fx:controller="de.uniks.ludo.controller.sub.DiceSubComponent">
    <Label fx:id="eyesLabel" text="🎲"/>
    ...
</fx:root>
```

In order to use the component in another controller, create a field in the controller and annotate it with `@SubComponent`.

```java
@Controller
public class IngameController {

    @SubComponent
    @Inject // Injected by Dagger
    public DiceSubComponent dice;

    // ...
}
```

Subcomponents will be initialized and rendered together with its parent controller.
For more information, see the [documentation](../../README.md#-sub-controllers).

The component can now be used as a JavaFX node and added to the view of the controller.
As the component is a javafx node, it can also be added in the FXML file directly.
See the [documentation](../../README.md#-sub-controllers-in-fxml) for more information.

```java
public class IngameController {

    @FXML
    @Inject
    @SubComponent
    DiceSubComponent diceSubComponent;

    @OnRender
    public void setupDice() {
        this.diceSubComponent.setOnMouseClicked(event -> {
            // ...
        });
    }
    
    // ...
}
```

```xml
<VBox fx:controller="de.uniks.ludo.controller.IngameController">
    <Label fx:id="playerLabel" text="%ingame.current.player" />
    <AnchorPane fx:id="boardPane" />
    <DiceSubComponent fx:id="diceSubComponent" />
</VBox>

```

### Conclusion

In this tutorial, we have learned how to start a new project using `FulibFx` and how to create controllers and views.
We have also learned how to configure the routing and how to pass parameters to controllers.
In addition, we have learned how to use resources, titles, subscribers and subcomponents.
For more information about the different features of `FulibFx`, see the [documentation](../../README.md).

---

[Overview](README.md)
