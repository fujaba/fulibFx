package io.github.sekassel.uno.controller;

import io.github.sekassel.uno.App;
import io.github.sekassel.uno.model.Player;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class GameOverController implements Controller {

    public static final String GAMEOVER_SCREEN_TITLE = "Uno - Game Over!";

    private final App app;
    private final Player player;

    @FXML
    public VBox gameOverScreen;
    @FXML
    public Button backToMenuButton;
    @FXML
    public Label wonLabel;

    public GameOverController(App app, Player player) {
        this.app = app;
        this.player = player;
    }

    @Override
    public String getTitle() {
        return GAMEOVER_SCREEN_TITLE;
    }

    @Override
    public void init() {
        // Nothing to init here
    }

    @Override
    public Parent render() {
        Parent rendered = loadControllerScreen(this, "view/gameover.fxml");

        wonLabel.setText(wonLabel.getText().formatted(player.getName()));

        return rendered;
    }

    @FXML
    /**
     * The method triggered by the quit button.
     * Displays the setup screen again.
     */
    private void backToMenu() {
        this.getApp().show(new SetupController(this.getApp()));
    }

    @Override
    public void destroy() {
        // Nothing to clean up here
    }

    /**
     * Returns the current screen (main pane for the controller).
     * This can be called before render() has been called to change things before the rendering.
     * It can also be used to get the pane without rendering again.
     *
     * @return The current screen (main pane for the controller)
     */
    public Pane getAsPane() {
        return this.gameOverScreen;
    }

    /**
     * @return The current App instance
     */
    public App getApp() {
        return this.app;
    }
}
