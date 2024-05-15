# Testing SubComponents

As subcomponents extend from JavaFX nodes mocking them destroys their functionality making them useless and prevents them from being rendered.
Spying has similar issues. Another problem with subcomponents is that they often require multiple dependencies like services themselves.

Therefor the best way of testing a subcomponent is by creating a field inside the controller test and annotating it with `@InjectMocks` so that all the dependencies are injected into it as well.
Since fields annotated with `@InjectMocks` cannot be injected into other fields annotated with the same annotation, this has to be done manually.

```java
@ExtendWith(MockitoExtension.class)
public class IngameControllerTest extends ControllerTest {

    @Spy
    GameService gameService;
    @InjectMocks
    DiceSubComponent diceSubComponent;
    // ...

    @InjectMocks
    IngameController ingameController;

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        ingameController.diceSubComponent = diceSubComponent; // Manually set the component instance
        app.show(ingameController, Map.of("playerAmount", 2));
    }

    @Test
    public void test() {
        // ...
    }
}
```
---

[⬅ Testing Controllers](2-controllers.md) | [Overview](README.md) | [Testing with Dagger ➡](4-dagger.md)