package de.uniks.ludo;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class AppTest extends ApplicationTest {

    @Spy
    public final App app = new App();

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        app.setComponent(DaggerTestComponent.builder().mainApp(app).build());
        app.start(stage);
        stage.requestFocus();
    }

    @Test
    public void test() {
        Platform.runLater(() -> app.stage().requestFocus());
        waitForFxEvents();

        press(KeyCode.LEFT);
        release(KeyCode.LEFT);
        clickOn("#startButton");

        waitForFxEvents();

        FxAssert.verifyThat("#playerLabel", (Label label) -> label.getText().contains("1"));

        roll();
        clickOn("#piece-1-3");
        roll();
        clickOn("#piece-1-3");
        roll();
        clickOn("#piece-1-3");

        FxAssert.verifyThat("#playerLabel", (Label label) -> label.getText().contains("2"));

        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-3");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        clickOn("#piece-1-2");
        roll();
        roll();
        roll();
        roll();
        roll();
        roll();
        roll();
        roll();
        roll();
        roll();
        clickOn("#piece-1-1");
        roll();
        clickOn("#piece-1-1");
        roll();
        clickOn("#piece-1-1");
        roll();
        roll();
        clickOn("#piece-1-1");
        roll();
        roll();
        clickOn("#piece-1-1");
        roll();
        roll();
        clickOn("#piece-1-1");
        roll();
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-1-1");
        roll();
        roll();
        clickOn("#piece-1-1");
        roll();
        roll();
        clickOn("#piece-1-0");
        roll();
        roll();
        clickOn("#piece-1-1");
        roll();
        clickOn("#piece-1-1");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-1-1");
        roll();
        clickOn("#piece-2-0");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-1-1");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-2-0");
        roll();
        clickOn("#piece-1-1");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-1-1");
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-2-0");
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-1-1");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-2-2");
        roll();
        clickOn("#piece-2-0");
        roll();
        clickOn("#piece-2-0");
        roll();
        clickOn("#piece-2-0");
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-2-0");
        roll();
        clickOn("#piece-1-0");
        roll();
        clickOn("#piece-2-0");
        roll();
        roll();
        clickOn("#piece-2-0");
        roll();
        roll();
        clickOn("#piece-2-0");
        roll();
        roll();
        clickOn("#piece-2-0");
        roll();
        roll();
        clickOn("#piece-2-0");
        roll();
        clickOn("#piece-1-0");

        waitForFxEvents();

        FxAssert.verifyThat("#playerWonLabel", Node::isVisible);

    }

    private void roll() {
        clickOn("#eyesLabel");
        sleep(1100);
    }

}
