# Data Flow

There are several methods for sending data between controllers and components. This tutorial provides an overview of the different possibilities and best practices for your application.

### Depending on your usecase, different sections are relevant to you:

- Do you need bidirectional data flow or data flow between multiple components?
  - Use [Properties](#properties).
- Do you want to pass data from a parent to its child?
  - Do you want to update the child at some later point after initialization/initial rendering?
    - Use [java methods](#java-constructs) like setters.
  - Use [parameters](#parameters).
- Do you want to pass data from a child to its parent?
  - Use [consumers/runnables](#callbacks).

## From parents to children

As components are usually used to split up functionality of controllers into smaller parts, e.g. for displaying certain parts of the view in a reusable way, they need to receive data.
This section covers different ways of sending data from a controller to its child components.

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

Since controllers and components are Java objects, basic concepts like constructor parameters, setters, and getters can also be used. After creating a component instance, methods can be called on it for passing data or other effects. Unlike parameters which can only pass data during lifecycle events, the child's methods can be called at any time.

```java
@Controller
public class MyController {

  MyComponent component;

  @OnRender
  void createSubs() {
    component = new MyComponent(new Foo());
    component.bar(new Data());
    // ...
  }

  void onButtonClicked() {
    component.foo();
  }

}
```

## From children to parents

Data should usually only flow from parents to children. A controller can pass data directly to its sub-components using the `@Param` annotation, but a child should not directly access the parent instance, ensuring reusability without dependency on the parent.

However, sending data from a child to the parent is sometimes necessary. For example, if a component contains a button that changes the screen color when pressed, the parent needs to be updated when the button is clicked. 

### Callbacks
An easy way of sending data from a child to a parent is creating a calback (e.g. a Consumer or Runnable) in the parent and passing it to the child.
The callback can then be called in the child to pass data to the parent.

```java
@Component
public class MyComponent {

  @Param("colorChange")
  Consumer<Color> color;

  void onButtonClicked() {
    Color newColor = ...;
    color.accept(newColor);
  }
}
```

```java
@Controller
public class MyController {

  @OnRender
  void createSubs() {
    Consumer<Color> colorChange = (color) -> System.out.println("New color is " + color);
    MyComponent component = app.initAndRender(new MyComponent(), Map.of("colorChange", colorChange));
    // ...
  }

}
```

The same approach can be used with Runnables instead of Consumers if only an effect needs to be triggered without passing any data.
Instead of the `@Param` annotation, callbacks can also be set using getters, setters, or constructor parameters, though that is not very advisable.

### Properties

For bidirectional data flow, Properties can be used.
Instead of defining a Runnable or Consumer directly, you can create a Property and then register listeners to it.

```java
@Component
public class MyComponent {
  // option 1: The subcomponent reuses the parent's property instance.
  @Param("color")
  ObjectProperty<Color> color;

  // Option 2: The subcomponent uses its own property instance and binds it to the parent's property.
  // Writing to this property will NOT update the parent's property.
  // We have to unbind using destroy.
  @Param(value = "color", method = "bind", type = Object.class)
  final ObjectProperty<Color> colorBind = new SimpleObjectProperty<>();

  // Option 3: The subcomponent uses its own property instance and binds it to the parent's property bidirectionally.
  // Writing to this property will update the parent's property.
  // We have to manually bind using subscriber to allow for unbinding.
  final ObjectProperty<Color> colorBindBidi = new SimpleObjectProperty<>();
  Subscriber subscriber = ...;
  
  @OnInit
  void init(@Param("color") ObjectProperty<Color> color) {
    subscriber.bindBidirectional(colorBindBidi, color);
  }

  void onButtonClicked() {
    Color newColor = ...;
    color.set(newColor);
  }
  
  @OnDestroy
  void destroy() {
    colorBind.unbind();
  }
}
```

```java
@Controller
public class MyController {

  Subscriber subscriber = ...;

  @OnRender
  void createSubs() {
    ObjectProperty<Color> color = new SimpleObjectProperty(Color.WHITE);
    MyComponent component = app.initAndRender(new MyComponent(), Map.of("color", color));
    subscriber.listen(color, (observable, oldValue, newValue) -> { // Use subscribers to prevent memory leaks
      System.out.println(newValue);
    });
  }

}
```

The same Property could also be passed to multiple components and if one updates its value, every other component can react to it.

If the field annotated with `@Param` is final and not null, the property will be bound instead of being overwritten.
This allows to react to changes, but changes made to the property in the child will not apply to the parent.

## Bad Practices

Avoid including a direct reference to a child's parent in the code, as this reduces reusability, making the component dependent on the parent.

```java
@Component
public class MyComponent {

  @Param("parent")
  MyController parent; // Bad, makes MyComponent depend on MyController

  void onButtonClicked() {
    Color newColor = ...;
    parent.setColor(newColor);
  }
}
```

Additionally, avoid using JavaFX's `getParent()` method. While it might work when the child is inside another component, it does not provide access to the controller instance if the parent is a controller, but only to its view element.

---

[Overview](README.md)
