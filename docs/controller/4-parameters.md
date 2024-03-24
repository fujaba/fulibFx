# Parameters [![Javadocs](https://javadoc.io/badge2/org.fulib/fulibFx/Javadocs.svg?color=green)](https://javadoc.io/doc/org.fulib/fulibFx/latest/org/fulib/fx/annotation/param/package-summary.html)

To pass parameters to a controller, an additional argument can be provided to the show method, consisting of a map of
strings and objects. The strings specify the argument's name and the objects are the value of the argument. For example,
`show("/route/to/controller", Map.of("key", value, "key2", value2))` will pass the value `value` to the argument `key`.

To use a passed argument in a field or method, you have to annotate it with `@Param("key")`. The name of the parameter
will be used to match it to the map of parameters passed to the `show()` method. If the annotation is used on a field,
the field will be injected with the value of the parameter before the controller is initialized. If the annotation is used
on a method, the method will be called with the value of the parameter before the controller is initialized. If the
annotation is used on a method parameter of a render/init method, the method will be called with the value of the parameter.

If `@Param` is used on a field containing a `WriteableValue` (e.g. a `StringProperty`), its value will be set to the
parameter's value if the parameter has the correct type (e.g. a `String` for a `StringProperty`). If the parameter is
a `WritableValue` as well, the logic will be the same as for a normal field.

Instead of accessing the parameters one by one, you can also use the `@ParamsMap` annotation to inject a map of all parameters.
This annotation can be used for fields and method parameters of type `Map<String, Object>`. If the annotated field is final,
`clear` and `putAll` will be called instead.

If you want to call a setter method with multiple parameters, you can use the `@Params` annotation to specify the names of
the parameters that should be passed to the method. This annotation can be used for methods with multiple parameters.
The order of the parameters in the method has to match the order of the names in the annotation.

In order to pass arguments to the following controller, the method `show("/route/to/controller", Map.of("fofo", myFoo, "baba", myBa))`
would have to be called. For more information on how to use the `show` method, see the [Routing](7-routing.md) section.

```java

@Controller
public class FooController {

    // The parameter 'baba' will be injected into this field before the controller is initialized
    @Param("baba")
    private Bar bar;

    // The set method of the writable value will be called with the parameter 'fofo'
    @Param("fofo")
    private ObjectProperty<Foo> foo = new SimpleObjectProperty<>();

    // This field will be injected with a map of all parameters before the controller is initialized
    @ParamsMap
    private Map<String, Object> params;

    @Params({"fofo", "baba"}) // This also works with @Param and @ParamsMap
    public void setFoo(Foo foo, Bar bar) {
        // This method will be called with the parameter 'fofo' and 'baba' before the controller is initialized
    }

    @onRender
    public void render(@Param("fofo") Foo foo, @ParamsMap Map<String, Object> params) {
        // This method will be called with the parameter 'fofo' and a map of all parameters upon rendering
    }

}
```

If a controller expects an argument but no argument with a suitable name is passed, `null` will be passed instead, except for fields which will be left unchanged ("default value").
Any arguments not expected by the controller will be ignored.

If an argument is provided, but the type doesn't match the type of the field or method parameter, an exception will be
thrown.

The order of injection is as follows:
1. Fields will be injected with `@Param` annotations and `@ParamsMap` annotations
2. Methods annotated with `@Param` will be called
3. Methods annotated with `@Params` will be called
4. Methods annotated with `@ParamsMap` will be called
5. The controller will be initialized (`@onInit`)
6. The controller will be rendered (`@onRender`)

---

[⬅ Views](3-views.md) | [Overview](README.md) | [Internationalization ➡](5-internationalization.md)