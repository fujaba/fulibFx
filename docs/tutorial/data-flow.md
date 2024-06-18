## Data Flow

There are several methods for sending data between controllers and components. This tutorial provides an overview of the different possibilities and best practices for your application.

### Parameters

The simplest method is using the `@Param` annotation, which can be employed when displaying a controller with the `show()` or `initAndRender()` methods. This annotation can also bind various properties, making the data flow responsive to changes.

```java
@Component
public class MyComponent {

  @Param("data")
  Data data;

  // ...
}
```

```java
@Controller
public class MyController {

  @OnRender
  void createSubs() {
    MyComponent component = app.initAndRender(new MyComponent(), Map.of("data", myData));
    // ...
  }

}
```

### Java Constructs

Since controllers and components are Java objects, basic concepts like constructor parameters, setters, and getters can also be used. After creating a component instance, methods can be called on it for passing data or other effects.

```java
@Controller
public class MyController {

  @OnRender
  void createSubs() {
    // ...
    MyComponent component = new MyComponent(new Foo());
    component.bar(new Data());
  }

}
```

## Best Practices

Data should only flow from parents to children. A controller can pass data directly to its sub-components using the `@Param` annotation, but a child should not directly access the parent instance, ensuring reusability without dependency on the parent.

However, sending data from a child to the parent is sometimes necessary. For example, if a component contains a button that changes the screen color when pressed, the parent needs to be updated when the button is clicked. This can be done by either using `@Param` with a property and adding a listener in the parent or by passing a callback to the child, which is called when the button is pressed.

```java
@Component
public class MyComponent {

  @Param("colorChange")
  ObjectProperty<Color> color;

  void onButtonClicked() {
    Color newColor = ...;
    color.set(newColor);
  }
}
```

```java
@Controller
public class MyController {

  @OnRender
  void createSubs() {
    ObjectProperty<Color> color = new SimpleObjectProperty(Color.WHITE);
    MyComponent component = app.initAndRender(new MyComponent(), Map.of("colorChange", color));
    color.addListener((observable, oldValue, newValue) -> {
      System.out.println("New color is " + newValue);
    });
  }

}
```

The same approach can be used with callbacks instead of properties if only an effect needs to be triggered.

Instead of the `@Param` annotation, callbacks or properties can also be set using getters, setters, or constructor parameters.

## Bad Practices

Avoid including a direct reference to a child's parent in the code, as this reduces reusability, making the component dependent on the parent.

```java
@Component
public class MyComponent {

  @Param("parent")
  MyController parent;

  void onButtonClicked() {
    Color newColor = ...;
    parent.setColor(newColor);
  }
}
```

Additionally, avoid using JavaFX's `getParent()` method. While it might work when the child is inside another component, it does not provide access to the controller instance if the parent is a controller, but only to its view element.
