# ☕ fulibFx 
[![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/javadoc.svg?style=for-the-badge&color=green)](https://javadoc.io/doc/org.fulib/fulibFx) ![Java 17/21](https://img.shields.io/github/actions/workflow/status/fujaba/fulibFx/java-ci-cd.yaml?style=for-the-badge&logo=github&color=green) [![GH Pages](https://img.shields.io/badge/GH_Pages-Click_here-green?style=for-the-badge&logo=github)](https://fujaba.github.io/fulibFx)

fulibFx is a framework for JavaFX applications adding different utilities and features.
It provides a simple way to create and manage controllers, views, routes, subcomponents, modals, and more.

The framework is built on top of JavaFX and uses Dagger for dependency injection and RxJava for reactive programming.
It also provides a few utility classes and data structures to simplify the creation of JavaFX applications.

## ❓ Support
If you have any questions or need help with the framework, feel free to open an [issue on GitHub](https://github.com/fujaba/fulibFx/issues).
When reporting errors, make sure to use the latest available version of the framework with a supported Java version (17 or 21).

Before reporting an issue, please check the [common issues](#-common-issues) section or the [wiki](https://fujaba.github.io/fulibFx/) first.

## 🔗 Installation

```groovy
repositories {
    mavenCentral()
}

dependencies {
    // Framework
    implementation 'org.fulib:fulibFx:VERSION'
    
    // Annotation processor
    annotationProcessor 'org.fulib:fulibFx-processor:VERSION'
}

compileJava {
    // Required for the annotation processor
    options.sourcepath = sourceSets.main.resources.getSourceDirectories()
}
```

## 📑 Features

The framework provides a variety of features to simplify the creation of JavaFX applications. The following list
provides an overview of the most important features:

### 🎮 Controllers and Components

[Controllers](docs/controller/README.md) can be created by annotating a class with [`@Controller`](docs/controller/1-controllers.md).
To specify methods which should be executed during the lifetime of the controller, they should be annotated with either 
[`@OnInit`, `@OnRender` or `@OnDestroy`](docs/controller/1-controllers.md) to specify their respective execution points.

A special type of controller is the [component](docs/controller/2-components.md). They can be used to create controller which can be used inside other
controllers. Components have to extend from a JavaFX Parent (or any class extending from Parent) and have to be annotated
with `@Component`. Using [`@SubComponent`](docs/controller/8-subcomponents.md) on a field containing a component will automatically initialize and render the
component along with the parent controller.

The framework also provides a way to pass [parameters](docs/controller/4-parameters.md) to a controller using `@Param` 
or utilities like setting a [title](docs/controller/6-titles.md) using `@Title` or registering 
[key events](docs/controller/10-key-events.md) `@OnKey`.

```java

@Title("Todo List")
@Controller("view/Todo.fxml")
public class TodoController {
    
    @FXML
    private VBox container;
    
    @SubComponent
    @Inject
    TodoListComponent todoListComponent;
    
    @OnInit
    public void initialize() {
        // Called when the controller is initialized
    }
    
    @OnRender
    public void render() {
        this.container.getChildren().add(todoListComponent);
    }
    
    @OnDestroy
    public void destroy() {
        // Called when the controller is being cleaned up
    }
    
    @OnKey(KeyCode.ENTER)
    public void onEnterPressed() {
        // Called when the enter key is pressed
    }
    
}
```

When using Dagger, add the following snippet module to provide the `FulibFxApp` to internal components using your app class.

```java
@Module
public class MainModule {
    
    // ...
    
    @Provides
    FulibFxApp app(YourApp app) {
        return app;
    }
}
```

### 📜 Routes

The framework provides a simple way to navigate between controllers using the `show` method. The method takes a route
as an argument and displays the corresponding controller. The routes can be specified using a router class which gets 
registered in the `FulibFxApp` class.

```java
public class Router {
    
    @Route("")
    Provider<HomeController> home;
    
    @Route("todo")
    Provider<TodoController> todo;
    
    @Route("todo/settings")
    Provider<SettingsController> settings;
    
}
```

### 🔄 Hot Reload

When developing an application, it is often necessary to reload the application to see the changes. The framework provides
a way to [refresh a controller's view](docs/features/3-history.md) without having to restart the whole application. 
This can be done by calling the `refresh` method of the `FulibFxApp` class.

<img width="640" height="360" src="docs/assets/hot-reload.gif" alt="GIF showing the hot reload feature">

### 🏭 Annotation Processor

The framework provides an annotation processor that hooks into the Java compiler.
It can check for various errors and warnings during compilation, allowing you to find problems early on without having to run the application.
Additionally, the annotation processor generates auxiliary classes that encapsulate some of the functionality of your controllers and components, like calling the initialization methods or providing the title.
This can greatly improve the performance at runtime and simplifies debugging by reducing stack traces and allowing you to debug the generated code.
The annotation processor is optional but strongly recommended.

### 🧷 Utilities

The framework provides a few utility classes and data structures to simplify the creation of JavaFX applications.

- [Subscriber](docs/features/1-subscriber.md): A class to simplify the creation of subscriptions and to automatically dispose them when the controller is destroyed.
- [For Loop](docs/features/2-for.md): A class for creating a node for each element in a list.
- [Internationalization](docs/controller/5-internationalization.md): A way to use resource bundles in your controller and app title.
- [Much more ...](docs/features/README.md)

## 📚 Wiki

The framework provides a comprehensive [wiki](docs/README.md) with detailed information about all features and how to use them.
It also includes a few examples to help you get started with the framework.

A list of all possible error codes can be found [here](ERROR_CODES.md).

## 🛑 Common issues

### 1. The framework throws an exception when doing something
All exceptions thrown by the framework are listed in the [error code documentation](ERROR_CODES.md). 
When the framework throws an exception, it will print the error code and a short description of the error to the console.
If for example the error `java.lang.IllegalArgumentException: FFX1000: Class '*' is not a component.` is thrown, you can check the error code documentation for [FFX1000](ERROR_CODES.md#1000-class--is-not-a-component) to find out what the error means and how to fix it.

### 2. My route is not found even though it is registered
When using `show("route/to/controller")` without a leading "`/`", the route is relative to the currently displayed controller. 
Meaning if you are currently displaying the controller `"/foo/bar"` and you call `show("baz")`, the route will be `"/foo/bar/baz"`.
If you want to display a controller from the root, you have to start the route with a "`/`".

### 3. Weird things happen during refresh
When refreshing a controller, the controller is destroyed and then reloaded with the same parameters as before. 
If an object has been passed as a parameter and the object has been modified during the lifetime of the controller,
the already modified object will be passed after the refresh, just to be modified again. This can lead to unexpected 
behaviour. To avoid this, you should try to not modify objects passed as parameters. Instead, you should create a copy 
of the object and modify the copy or modify the object before passing it to the controller.

### 4. The framework doesn't compile even though the view file exists
The framework uses an [annotation processor](#-annotation-processor) to check if the view file exists.
If the view file is not found, the processor will throw an error.
Please make sure that the view file is in the correct location and that `options.sourcepath` is set correctly in your `compileJava` task (see [Installation](#-installation)).

### 5. The SceneBuilder doesn't recognize my controller
When using the SceneBuilder, it might not recognize your controller's FXML file. This can happen when the FXML file contains
elements which are not present in the basic JavaFX library. To fix this, you can add a jar file containing the missing
elements to the SceneBuilder. The simplest way is to build your project and then add the jar file by clicking on the small
gear icon next to the search bar and selecting "JAR/FXML Manager". Then you can add the jar file by clicking on the "Add
Library/FXML from file system" button.

### 6. My tests fail
Tests using TestFX (especially headless) are janky in general. This isn't really a problem of the framework but rather a problem of TestFX itself.
Some tips to avoid problems:
- Set the stage size to a fixed size (e.g. 1600x900) to avoid out of view elements.
- Use `stage.requestFocus()` to focus the stage before running the test.
- Minimize other windows to avoid interference (yes, I know this is stupid).
