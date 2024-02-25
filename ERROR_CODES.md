# Error codes and messages

## Controller/Component

### 1000: `Class '*' is not a component.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when `initAndRender` is called on an element that is not a component.

### 1001: `Class '*' is not a controller or component.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework tries to render something that is not a component or controller.
This error should never be thrown if the framework is used correctly.

### 1002: `Method '*()' in class '*' does not return a (subtype of) 'javafx.scene.Parent'.`
- Runtime: ✅
- Annotation Processor: ✅

This error is thrown when a method is used to create the view of a component, but the method doesn't return a `Parent`
object.

```java

@Controller("#getView")
public class MyController {

    public String getView() {
        // Wrong, should return a Parent object (or a subclass of it)
        return "This is not a Parent object";
    }
}
```

### 1003: `Method '*()' in class '*' does not exist.`
- Runtime: ✅
- Annotation Processor: ✅

This error is thrown when a method is used to create the view of a component, but the method does not exist.

```java

@Controller("#getView")
public class MyController {

    // Wrong, should be named getView (or change the name in the annotation)
    public VBox myView() {
        return new VBox();
    }
}
```

### 1004: `Could not call method '*' in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the method specified in the `@Controller` annotation cannot be called successfully.
This can happen if the method throws an exception.

### 1005: `Could not call method '*' annotated with '*' in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if a method annotated with an event annotation (onInit, onRender, onDestroy, etc.) cannot be called
successfully.
This can happen if the method throws an exception.

### 1006: `Components must extend (a subtype of) 'javafx.scene.Parent'.`
- Runtime: ❌
- Annotation Processor: ✅

This error is thrown when the framework finds a class annotated with `@Component` that is not a subclass of `Parent`.

```java

@Component
public class MyController { // Wrong, should extend Parent (or a subclass of it)

}
```

### 1007: `Class '*' is annotated with both @Controller and @Component.`
- Runtime: ❌
- Annotation Processor: ✅

This error is thrown when the framework finds a class annotated with both `@Controller` and `@Component`.

```java

@Component
@Controller // Wrong, should be one or the other
public class MyComponent extends VBox {

}
```

### 1008: `Method '*' in class '*' must not have any arguments.`
- Runtime: ✅
- Annotation Processor: ✅

This error is thrown when the method specified in the `@Controller` annotation has arguments.

```java

@Controller("#getView")
public class MyController {

    // Wrong, must not have arguments
    public VBox getView(String args) {
        return new VBox();
    }
}
```

## Resources

### 2000: `Could not find resource '*'.`
- Runtime: ✅
- Annotation Processor: ✅

This error is thrown when the framework tries to load a resource that does not exist.
This can happen if the resource is not in the correct location or if the name is misspelled.

### 2001: `File '*' exists, but could not be converted to URL.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework tries to load a resource that exists, but cannot be linked back to its original
file.
The only situation where this error should be thrown is when the framework uses the file instead of the resource when in
development mode.

### 2002: `Couldn't load the FXML file for controller/component '*'`
- Runtime: ✅
- Annotation Processor: ❌

This error occurs when there is a problem in the FXML file for a component or controller.
This can happen if the FXML file is not valid or if there is a problem with the file.

### 2003: `Class '*' has more than one field annotated with @Resource.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when there are multiple fields annotated with `@Resource` in a single controller or component.

```java

@Controller
public class MyController {

    @Resource
    ResourceBundle bundle1;

    @Resource // Wrong, should not have multiple fields annotated with @Resource
    ResourceBundle bundle2;
}
```

### 2004: `Field '*' in class '*' annotated with @Resource is not of type ResourceBundle.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if there is a field annotated with `@Resource` that is not a `ResourceBundle`.

```java

@Controller
public class MyController {

    @Resource // Wrong, should be a ResourceBundle (or a subclass of it)
    List<String> bundle;
}
```

### 2005: `Couldn't access the resource bundle field '*' in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework fails to access a field as the resource bundle.
This can happen if the field isn't initialized.

### 2006: `Maybe the source path has not been set, see https://stackoverflow.com/a/74159042.`
- Runtime: ❌
- Annotation Processor: ✅

This error is a note for FFX2000 giving more information about the problem.
If the source path isn't set in the build.gradle, the framework will not be able to find the FXML files,
see https://stackoverflow.com/a/74159042.

## Routes

### 3000: `Class '*' has already been registered as the router class.`
- Runtime: ✅
- Annotation Processor: ❌

The error is thrown when a router class is used to register routes even though another class is already registered as
the router.

### 3001: `Field '*' is not annotated with @Route`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the framework tries to register a field as a route provider even though it is not annotated
with `@Route`.
This should never happen if the framework is used correctly.

### 3002: `Route '*' already leads to '*' but was tried to be registered for '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when a route is registered with a path that is already in use.

```java
public class Routes {

    @Route("/path")
    Provider<MyController> myController;

    @Route("/path") // Wrong, should not have the same path as another route
    Provider<OtherController> otherController;
}
```

### 3003: `Field '*' in '*' is annotated with @Route but is not a Provider<T> providing a controller/component.`
- Runtime: ✅
- Annotation Processor: ✅

This error is thrown when a field annotated with `@Route` is not a `Provider` of a `Controller` or `Component`.

```java
public class Routes {

    @Route("/path")
    Provider<String> provider; // Wrong, should be a Provider of a Controller or Component
}
```

### 3004: `Field '*' in class '*' is not a valid provider field.`
- Runtime: ✅
- Annotation Processor: ✅

This error is thrown when a field annotated with `@Route` is not a `Provider`.

```java
public class Routes {

    @Route("/path")
    MyController controller; // Wrong, should be a Provider (e.g. Provider<MyController>)
}
```

### 3005: `Route '*' could not be found.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework tries to access a route that does not exist.

## Parameters

### 4000: `Couldn't fill parameter '*' into field '*' in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework fails to put a parameter value into a field.

### 4001: `Couldn't call setter method with parameter '*' for field '*' in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework fails to call the set method of a property field with the parameter value.

### 4002: `Field '* 'annotated with @ParamsMap in class '*' is not of type Map<String, Object>.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when a field is annotated with `@ParamsMap` but is not a `Map<String, Object>` or a subclass of it.

```java

@Controller
public class MyController {

    @ParamsMap // Wrong, should be a Map<String, Object> (or a subclass of it)
    List<String> params;
}
```

### 4003: `Method '*' annotated with @ParamsMap in class '*' must have exactly one parameter of type Map<String, Object>.`
- Runtime: ✅
- Annotation Processor: ✅

This error is thrown when a method annotated with `@ParamsMap` has other arguments than a single `Map<String, Object>`.

```java

@Controller
public class MyController {

    @ParamsMap
    public void myMethod(Map<String, Object> params, String otherArg) { // Wrong, should not have other arguments
        // ...
    }
}
```

### 4004: `Parameter '*' annotated with @ParamsMap in method '*' in class '*' is not of type Map<String, Object>.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework tries to call an event method with parameters, but the argument annotated
with `@ParamsMap` is not a `Map<String, Object>` or a subclass of it.

```java

@Controller
public class MyController {

    @OnInit
    public void myMethod(@ParamsMap List<String> params) { // Wrong, should be a Map<String, Object> (or a subclass of it)
        // ...
    }
}
```

### 4005: `Couldn't fill parameter '*' into method '*' in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework fails to call a method with a parameter (`@Param`).
This can happen if the method throws an exception.

### 4006: `Method '*' in class '*' has a different amount of parameters than the annotation provides.`
- Runtime: ✅
- Annotation Processor: ✅

This error is thrown if a method is annotated with `@Params` but the amount of arguments differs from the amount of
parameters in the annotation.

```java

@Controller
public class MyController {

    @Params({"param1", "param2", "param3"})
    public void myMethod(String string, Integer number) { // Wrong, should have 3 arguments
        // ...
    }
}
```

### 4007: `Parameter '*' in field '*' in class '*' is of type '*' but the provided value is of type '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the type of the parameter value does not match the type of the field.

### 4008: `Parameter '*' in method '*' in class '*' is of type '*' but the provided value is of type '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the type of the parameter value does not match the type of the method argument.

### 4009: `Parameter '*' in method '*' in class '*' is annotated with both @Param and @Params.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if a method argument is annotated with both `@Param` and `@ParamsMap`.

```java

@Controller
public class MyController {

    @OnInit
    public void myMethod(@ParamsMap @Param("param1") Map<String, Object> params) { // Wrong, should not have both annotations for the same argument
        // ...
    }
}
```

### 4010: `Couldn't fill parameter map into field '*' in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework fails to put the parameter map into a field.

### 4011: `Couldn't fill parameters into method '*' in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework fails to call a method with parameters (`@Params`).
This can happen if the method throws an exception.

## Queue

### 5000: `No previous element saved.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if there is no previous element in the queue.

### 5001: `No next element saved.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if there is no next element in the queue.

## Subcomponents

### 6000: `Multiple subcomponent providers annotated with @SubComponent with the same type '*' found in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown when the framework finds multiple providers of the same type annotated with `@SubComponent` in a
single controller or component.
This is forbidden as the framework cannot know which provider to use when creating subcomponent instances for the FXML
file.

```java

@Controller(view = "myView.fxml")
public class MyController {

    @SubComponent
    Provider<MySubComponent> subComponent1;

    @SubComponent // Wrong, should not have multiple providers of the same type
    Provider<MySubComponent> subComponent2;
}
```

### 6001: `Couldn't access the provider '*' in '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the framework fails to access a provider to create a subcomponent instance for the FXML file.
This can happen if the provider is not initialized.

### 6002: `Couldn't access field '*' annotated as a subcomponent in '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the framework fails to access a field to use as a subcomponent instance for the FXML file.
This can happen if the field is not initialized.

### 6003: `No usable instance of the subcomponent with type '*' in class '*' found.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if an FXML file requests a subcomponent but there are no remaining available instances provided by
the controller or component.
If the FXML uses a subcomponent multiple times, the controller or component must provide multiple instances of the
subcomponent or a provider thereof.

### 6004: `No instance of the subcomponent with type '*' in class '*' found.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if an FXML file requests a subcomponent that is not provided by the controller or component.

### 6005: `Field '*' in class '*' is annotated with @SubComponent but is not a subcomponent or provider thereof."`
- Runtime: ✅
- Annotation Processor: ✅

This error is thrown if the `@SubComponent` annotation is used on a field that is not a controller or component or a
provider thereof.

```java
public class MyController {

    @SubComponent
    VBox box; // Wrong, should be a controller, component or a provider thereof
}
```

### 6006: `Couldn't determine the type of the provider '*' in '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the framework fails to determine the type a provider provides.

## Loops

### 7000: `For loop is already initialized.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if a `For` loop is initialized twice.

### 7001: `Item '*' is already in the list.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if an item is added to a `For` loop twice.

# File Watcher

### 8000: `Couldn't start file service.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the file watcher fails to watch a directory.

### 8001: `Couldn't close watcher.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if closing the file watcher fails.

## Reflection

### 9000: `Couldn't run method for field '*' in class '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the framework fails to run a method for a certain field.
This error is internal and doesn't have a specific cause.

### 9001: `Couldn't access field '*' in '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the framework fails to access a field.

### 9002: `Field '*' in '*' is not initialized.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the framework tries to get a value from an uninitialized field.

### 9003: `Couldn't access getChildren() method in '*' or super classes.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the framework fails to get the `getChildren()` method of an object.
This can happen if the object is not a subclass of `Parent`.
This should never happen if the framework is used correctly.

## Other

### 10000: `Cannot traverse to parent of root node.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if one tries to go to the parent of a node without a parent in a tree structure.
This can happen if one uses `show("../")` whilst already being at the empty route.

### 10001: `No duplicator registered for '*'.`
- Runtime: ✅
- Annotation Processor: ❌

This error is thrown if the framework fails to create a copy of an object because no duplicator has been registered for
the object's class.