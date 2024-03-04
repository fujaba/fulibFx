# Internationalization

In order to use resource bundles in your controller's FXML file, you have to provide an instance of the resource bundle to the
framework. This can be done by creating a field containing your instance (e.g. with Dagger) and annotating it with `@Resource`.
When the corresponding controller is rendered, the framework will automatically set the resource bundle as the resource
bundle of the FXML file.

To set a default resource bundle once, you can use the `setDefaultResourceBundle` method of the `FulibFxApp` class.
The resource bundle will be used if no resource bundle has been specified for the controller.

```java
@Controller
public class TodoController {

    @Resource
    private ResourceBundle resourceBundle; // Could be provided by Dagger

    // ...
}
```

---

[⬅ Parameters](4-parameters.md) | [Titles ➡](6-titles.md)