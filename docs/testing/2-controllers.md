# Testing Controllers

In the following section, you will learn how to test a basic controller using TestFX and Mockito.

## ControllerTest

Testing controllers using TestFX requires the test to extend from `ApplicationTest`.
It is however recommended to create a helper class like `ControllerTest` extending `ApplicationTest`.
This class will contain some common code to reduce the amount of boilerplate required for each controller test.

```java
public class ControllerTest extends ApplicationTest {
    
    @Spy
    protected App app = new App();
    @Spy 
    protected ResourceBundle resources = ...; // Define common instances here and mock/spy them

    protected Stage stage; // Useful for checking the title for example

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        this.stage = stage;
        stage.setX(0); // This fixes potential issues in headless environments
        stage.setY(0);
        app.start(stage);
        stage.requestFocus(); // Make the test use the correct stage
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        app.stop();
        app = null;
        stage = null;
    }
}
```

The main annotations offered by Mockito are `@Spy` and `@Mock`. 
Mocking an instance completely removes all default behaviour and content of methods, fields and such, resulting in an empty shell which can later be redefined.
This is useful if the real behaviour isn't needed at all, but the instance itself has to exist.
Spying an instance doesn't touch the default behaviour but allows redefining parts of the logic and checking whether methods have been called using `verify`.

Spies and Mocks can later be injected into the controller instance which is being tested using `@InjectMocks`.

## Writing a real test

Since most of the setup is already defined in the `ControllerTest` class we can just extend it for our own tests.
In order to get Mockito working, the class has to be annotated with `@ExtendWith(MockitoExtension.class)`.

```java
@ExtendWith(MockitoExtension.class)
public class SetupControllerTest extends ControllerTest {

    @InjectMocks
    SetupController setupController;

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage); // It is important to call super.start(stage) to setup the test correctly
        app.show(setupController);
    }

    @Test
    public void test() {
        // Since we don't really want to show a different controller, we mock the show() method's behaviour to just return null
        doReturn(null).when(app).show(any(), any());

        assertEquals("Ludo - Set up the game", app.stage().getTitle());

        // TestFX offers different methods for interacting with the application
        moveTo("2");
        moveBy(0, -20);
        press(MouseButton.PRIMARY);
        release(MouseButton.PRIMARY);
        clickOn("#startButton");

        waitForFxEvents(); // Wait for the logic to run
        
        // Mockito can be used to check if the show() method was called with certain arguments
        verify(app, times(1)).show("ingame", Map.of("playerAmount", 2));

    }

}
```

Whenever something is loading asynchronously the method `waitForFxEvents()` should be called before checking the results.
This ensures that all JavaFX events have been run before continuing the tests.
Another way of waiting is the `sleep()` method, which allows to wait for a predefined time.
This is not recommended though as the defined time is either too long or too short and therefore can cause issues or unnecessary delays.

---

[⬅ Setup](1-setup.md) | [Overview](README.md) | [Testing SubComponents ➡](3-subcomponents.md)