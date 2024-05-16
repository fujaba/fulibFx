package de.uniks.ludo.controller;

import de.uniks.ludo.ControllerTest;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SetupControllerTest extends ControllerTest {

    @InjectMocks
    SetupController setupController;

    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        app.show(setupController);
    }

    @Test
    public void test() {
        doReturn(new VBox()).when(app).show(any(), any());

        assertEquals("Ludo - Set up the game", app.stage().getTitle());

        moveTo("2");
        moveBy(0, -20);
        press(MouseButton.PRIMARY);
        release(MouseButton.PRIMARY);
        clickOn("#startButton");


        verify(app, times(1)).show("ingame", Map.of("playerAmount", 2));

    }

}
