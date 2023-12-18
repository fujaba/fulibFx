package io.github.sekassel.uno.controller;

import io.github.sekassel.uno.App;
import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.model.Game;
import io.github.sekassel.uno.util.Utils;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class SetupController implements Controller {

    public static final String SETUP_SCREEN_TITLE = "Uno - Setup";

    private final App app;

    private String initialText; // The initial text that will be display in the name input
    private Integer initialBotAmount; // The initial bot amount that will be selected

    @FXML
    public VBox setupScreen;
    @FXML
    public Slider botAmountSlider;
    @FXML
    public TextField nicknameField;
    @FXML
    public Button playButton;

    public SetupController(App app) {
        this.app = app;
    }

    @Override
    public String getTitle() {
        return SETUP_SCREEN_TITLE;
    }

    @Override
    public void init() {
        // Nothing to init here
    }

    @Override
    public Parent render() {
        Parent rendered = loadControllerScreen(this, "view/setup.fxml");

        // Display initial values if set
        if (initialText != null)
            nicknameField.setText(initialText);

        if (initialBotAmount != null) {
            botAmountSlider.setValue(initialBotAmount);
        }

        setupPlayButton();

        return rendered;
    }

    /**
     * Changes the play button to only be active if a name is present
     */
    private void setupPlayButton() {
        playButton.disableProperty().bind(nicknameField.textProperty().isEmpty());
    }

    /**
     * The method triggered by the start button.
     * Creates a new game, modifies the username and displays the screen
     */
    @FXML
    private void start() {
        String name = Utils.trim(nicknameField.getText(), Constants.MAX_NAME_LENGTH);
        name = Utils.replaceIllegals(name);
        Game game = this.getApp().getGameService().createGame(name, (int) botAmountSlider.getValue());
        this.getApp().show(new IngameController(this.getApp(), game));
    }

    @Override
    public void destroy() {
        // Nothing to clean up here
    }

    /**
     * Sets the text that will be displayed as an initial value in the name input
     *
     * @param text The name that will be displayed
     * @return The current SetupController instance
     */
    public SetupController setInitialText(String text) {
        this.initialText = text;
        return this;
    }

    /**
     * Sets the amount of bots that will be displayed as an initial value
     *
     * @param amount The amount of bots that will be displayed
     * @return The current SetupController instance
     */
    public SetupController setInitialBotAmount(int amount) {
        this.initialBotAmount = amount;
        return this;
    }

    /**
     * Returns the current screen (main pane for the controller).
     * This can be called before render() has been called to change things before the rendering.
     * It can also be used to get the pane without rendering again.
     *
     * @return The current screen (main pane for the controller)
     */
    public Pane getAsPane() {
        return this.setupScreen;
    }

    /**
     * @return The current App instance
     */
    public App getApp() {
        return this.app;
    }
}
