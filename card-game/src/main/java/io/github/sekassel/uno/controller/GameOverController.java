package io.github.sekassel.uno.controller;

import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
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
        System.out.println(this + " created.");
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


    @onInit
    public void initSout() {
        System.out.println(this + " initialized.");
    }

    @onRender()
    public void renderSout() {
        System.out.println(this + " rendered.");
    }

    @onDestroy
    public void destroySout() {
        System.out.println(this + " destroyed.");
    }

}
