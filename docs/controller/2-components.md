# Components

Components are a special type of controller that can be used to create reusable components. Components have to extend
from a JavaFX Parent (or any class extending from Parent) and have to be annotated with `@Component`.

```java

@Component
public class MyComponent extends VBox {
}
```

Components can be used in the same way as controllers (including event annotations), but they can also be used as
subcomponents inside other controllers. As components are much more versatile than controllers, they are the recommended
way for creating your application.