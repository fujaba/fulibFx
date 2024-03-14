# Components [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/annotation/controller/Component.html)

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

---

[⬅ Controllers](1-controllers.md) | [Overview](README.md) | [Views ➡](3-views.md)