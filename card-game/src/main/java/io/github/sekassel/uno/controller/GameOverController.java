package io.github.sekassel.uno.controller;

import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import io.github.sekassel.jfxframework.annotation.param.Param;
import io.github.sekassel.uno.App;
import io.github.sekassel.uno.model.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Controller
public class GameOverController implements Titleable {

    public static final String GAMEOVER_SCREEN_TITLE = "Uno - Game Over!";

    @Inject
    App app;

    @FXML
    public VBox gameOverScreen;
    @FXML
    public Button backToMenuButton;
    @FXML
    public Label wonLabel;

    @Inject
    public GameOverController() {
    }

    @Override
    public String getTitle() {
        return GAMEOVER_SCREEN_TITLE;
    }

    // This method will be called when the controller is rendered. Since we don't have to save the winner in a field,
    // we can just pass it as a parameter to the method.
    @onRender
    public void render(@Param(value = "winner") Player player) {
        wonLabel.setText(wonLabel.getText().formatted(player.getName()));
    }

    /**
     * The method triggered by the quit button.
     * Displays the setup screen again.
     */
    @FXML
    private void backToMenu() {
        this.app.show("/");
    }

}
