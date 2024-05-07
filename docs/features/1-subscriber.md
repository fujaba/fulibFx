# Subscriber [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/controller/Subscriber.html)

By creating a new `Subscriber` instance (or by using dependency injection to provide one) and using its utility methods,
you can easily manage subscriptions without having to worry about disposing them one by one. Using `subscriber.dispose()`
will dispose all subscriptions added to the subscriber. When running in dev mode, destroying a controller will check if
all subscriber fields have been disposed and will print a warning if not.

Subscribers contain different methods for managing elements from RxJava, JavaFX and java beans (e.g. when using 
[Fulib](https://github.com/fujaba/fulib)).

```java

@Controller
public class TodoController {

    @Inject
    Subscriber subscriber;
    
    @Inject
    TodoService todoService;

    // ...

    @OnRender
    public void render() {
        this.subscriber.subscribe(this.todoService.getTodos(), todos -> {
            // Do something with the todos
        }); 
        this.subscriber.subscribe(() -> {
            // Add custom logic to be executed when the controller is destroyed
        });
    }
    
    @OnDestroy
    public void destroy() {
        this.subscriber.dispose();
    }
    
}
```

---

[Overview](README.md) | [For ➡](2-for.md)
