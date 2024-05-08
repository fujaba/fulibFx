# Controller [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/annotation/controller/Controller.html) 

Controllers are the backbone of your application. To set up a controller, create a class where the controller logic will
be defined and annotate it with `@Controller`.

```java

@Controller
public class TodoController {
    // ...
}
```

### Events [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/annotation/event/package-summary.html)

Within your controller class, you have the ability to define methods that are automatically triggered when the
controller is initialized, rendered or destroyed. These methods should be annotated with either `@OnInit`, `@OnRender`
or `@OnDestroy` to specify their respective execution points. The annotations have an optional parameter to specify the
order in which the methods should be called. The default order is 0 if no value is provided.

```java

@Controller
public class TodoController {
    
    // ...

    @OnInit
    public void thisMethodWillBeCalledOnInit() {
        // Called when the controller is initialized
    }

    @OnRender(0)
    public void thisMethodWillBeCalledOnRender() {
        // Called when the controller has been loaded and is ready to be displayed
    }
    
    @OnRender(1)
    public void thisMethodWillBeCalledOnRenderButLater() {
        // Called when the controller has been loaded and is ready to be displayed
        // This method will be called after the previous one
    }

    @OnDestroy
    public void thisMethodWillBeCalledOnDestroy() {
        // Called when the controller is being cleaned up
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

### Inheritance
Controllers can inherit from other classes. This can be useful to share common functionality between multiple controllers.
All annotations used in the parent class will be used in the child class as well (e.g. `@OnInit`, `@OnRender`, `@OnDestroy`, `@Param`, ...).

Overriding event methods is also possible, allowing for standardized methods to be specified.
If a child class overrides an event method, the child method should not be annotated with the event annotation.

```java
public class BaseController {
    
    // This subscriber will be used in all child classes
    Subscriber subscriber = ...;

    @OnDestroy
    public void cleanup() {
        // Cleanup the subscriber once instead of in every child class
        subscriber.dispose();
    }
    
}

@Controller
public class TodoController extends BaseController {
    
    // ...
    
    @OnInit
    public void init() {
        subscriber.subscribe(todoService.getTodos(), this::updateTodos);
    }
    
}
```

### Destroying controllers

When a controller is no longer needed, it should be destroyed to free up resources. This will automatically happen when
a new controller is shown using the `show()` method. However, if you for example subscribe to observables, the framework
will not clear them up them automatically. You should therefore save the disposables of your subscriptions and dispose them
when the controller is destroyed.

This can be done by creating a `CompositeDisposable`, adding all disposables to it and then calling `compositeDisposable.dispose()`
in a `@OnDestroy` annotated method.

The framework also provides utility classes for dealing with subscriptions and other mechanisms requiring cleanup.
One example for this is the [`Subscriber` class](../features/1-subscriber.md).


---

[Overview](README.md) | [Components ➡](2-components.md)
