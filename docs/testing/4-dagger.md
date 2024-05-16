# Testing with Dagger

When using Dagger inside the application, testing the app requires a testcomponent to be present.
This component contains all the dependencies the main module provides, but modified in a way that doesn't require a connection for example.

The component itself can just extend the main component and then use modules to override certain dependencies.
Inside the modules, Mockito methods such as `spy()` and `mock()` can be used to create the required instances. 
If specific behaviour is required, the instances can also be created manually.

```java
@Component(modules = {MainModule.class, TestModule.class})
@Singleton
public interface TestComponent extends MainComponent {

    @Component.Builder
    interface Builder extends MainComponent.Builder {
        TestComponent build();
    }
}
```

```java
@Module
public class TestModule {

    @Provides
    GameService gameService() {
        return new GameService(new Random(42));
    }

}
```

Now that the component and modules exist, we have to create a way of setting the component our app uses.
This step however is dependent on how the application is structured.
The easiest way is to create a setter method and call it, before the app starts.

```java
// ...
protected TestComponent testComponent;

@Override
public void start(Stage stage) throws Exception {
    super.start(stage);
    this.testComponent = (TestComponent) DaggerTestComponent.builder().mainApp(app).build();
    app.setComponent(testComponent);
    app.start(stage);
    stage.requestFocus();
}

// ...
```

The component instance makes it possible to inject services from test classes e.g. AppTest to redefine their behavior.

```java
public class AppTest extends ControllerTest {
    // ...
    
    @BeforeEach
    void setup() {
        final AuthApiService authApiService = testComponent.authApiService();
        // ...
    }
    
    // ...
}
```

---

[⬅ Testing SubComponents](3-subcomponents.md) | [Overview](README.md)