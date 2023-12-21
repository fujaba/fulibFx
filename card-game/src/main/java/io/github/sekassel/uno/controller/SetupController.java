package io.github.sekassel.uno.controller;

import io.github.sekassel.jfxframework.controller.Subscriber;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Param;
import io.github.sekassel.uno.App;
import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.model.Game;
import io.github.sekassel.uno.service.GameService;
import io.github.sekassel.uno.util.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Controller
@Singleton
public class SetupController implements Titleable {

    public static final String SETUP_SCREEN_TITLE = "Uno - Setup";

    @FXML
    public Slider botAmountSlider;
    @FXML
    public TextField nicknameField;
    @FXML
    public Button playButton;

    @Inject
    GameService gameService;
    @Inject
    App app;
    @Inject
    Subscriber subscriber;

    @Param("initialText")
    private String initialText; // The initial text that will be display in the name input
    @Param("initialBotAmount")
    private Integer initialBotAmount; // The initial bot amount that will be selected

    @Inject
    public SetupController() {
    }

    @Override
    public String getTitle() {
        return SETUP_SCREEN_TITLE;
    }

    @ControllerEvent.onRender
    public void render() {

        // Display initial values if set
        if (initialText != null)
            nicknameField.setText(initialText);

        if (initialBotAmount != null) {
            botAmountSlider.setValue(initialBotAmount);
        }

        setupPlayButton();
    }

    /**
     * Changes the play button to only be active if a name is present
     */
    private void setupPlayButton() {
        playButton.disableProperty().bind(nicknameField.textProperty().isEmpty());
        subscriber.addDestroyable(() -> playButton.disableProperty().unbind());
    }

    /**
     * The method triggered by the start button.
     * Creates a new game, modifies the username and displays the screen
     */
    @FXML
    private void start() {
        String name = Utils.trim(nicknameField.getText(), Constants.MAX_NAME_LENGTH);
        name = Utils.replaceIllegals(name);
        Game game = this.gameService.createGame(name, (int) botAmountSlider.getValue());
        this.app.show("/ingame", Map.of("game", game));
    }

    @ControllerEvent.onDestroy
    public void destroy() {
        // Remove the card from the game
        this.subscriber.destroy();
    }


}
