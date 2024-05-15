package de.uniks.ludo.controller;

import de.uniks.ludo.ControllerTest;
import de.uniks.ludo.controller.sub.DiceSubComponent;
import de.uniks.ludo.service.GameService;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.fulib.fx.controller.Subscriber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.util.NodeQueryUtils.hasText;

@ExtendWith(MockitoExtension.class)
public class IngameControllerTest extends ControllerTest {

    @Spy
    ResourceBundle bundle = ResourceBundle.getBundle("de/uniks/ludo/lang/lang", Locale.ENGLISH);
    @Spy
    GameService gameService;
    @Spy
    Subscriber subscriber;
    @InjectMocks
    DiceSubComponent diceSubComponent;

    @InjectMocks
    IngameController ingameController;

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        ingameController.diceSubComponent = diceSubComponent;
        app.show(ingameController, Map.of("playerAmount", 2));
    }

    @Test
    public void test() {

        // Check if the stage is created successfully
        assertEquals("Ludo - Ludo", app.stage().getTitle());
        verifyThat("#playerLabel", (Label label) -> label.getText().contains("1"));

        // Check if the players are created
        verifyThat("#piece-1-1", Node::isVisible);
        verifyThat("#piece-2-1", Node::isVisible);
        assertFalse(lookup("#piece-3-1") instanceof Circle);
        assertFalse(lookup("#piece-4-1") instanceof Circle);

        // Check if the dice is created and the roll animation works
        when(gameService.rollRandom()).thenReturn(6);
        verifyThat("#eyesLabel", (Label label) -> label.getTextFill() == Color.RED);
        clickOn("#eyesLabel");
        sleep(1100);
        verifyThat("#eyesLabel", hasText("6"));

        // Check if moving the player displays the correct fields
        moveTo("#piece-1-0");
        verifyThat("#field-0-4", node -> node.getEffect() != null);
        clickOn("#piece-1-0");
        verifyThat("#field-0-4", node -> node.getEffect() == null);

        // As the player rolled a 6, the player should be able to move again
        verifyThat("#playerLabel", (Label label) -> label.getText().contains("1"));

        // Roll a 3 and move the player
        when(gameService.rollRandom()).thenReturn(3);
        clickOn("#eyesLabel");
        sleep(1100);
        verifyThat("#eyesLabel", hasText("3"));
        moveTo("#piece-1-0");
        verifyThat("#field-3-4", node -> node.getEffect() != null);
        clickOn("#piece-1-0");
        verifyThat("#field-3-4", node -> node.getEffect() == null);

        // As the player rolled a 6, the player should be able to move again
        verifyThat("#playerLabel", (Label label) -> label.getText().contains("2"));
        verifyThat("#eyesLabel", (Label label) -> label.getTextFill() == Color.BLUE);
    }

}
