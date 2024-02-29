package de.uniks.ludo.controller;

import de.uniks.ludo.ControllerTest;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(MockitoExtension.class)
public class GameOverControllerTest extends ControllerTest {
    @Spy
    ResourceBundle bundle = ResourceBundle.getBundle(
            "de/uniks/ludo/lang/lang",
            Locale.ENGLISH
    );

    @InjectMocks
    GameOverController gameOverController;

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        app.show(gameOverController, Map.of("winner", 1));
    }

    @Test
    public void test() {

        assertEquals("Ludo - Game over!", app.stage().getTitle());
        verifyThat("#playerWonLabel", (Label label) -> label.getText().contains("1"));

    }

}
