package de.uniks.ludo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.Title;

import javax.inject.Inject;
import java.util.Map;

@Title("%setup.title")
@Controller
public class SetupController extends BaseController {

    @FXML
    public Button startButton;
    @FXML
    private Slider playerAmountSlider;

    @Inject
    public SetupController() {
    }

    @FXML
    public void onPlayClick() {
        int playerAmount = (int) this.playerAmountSlider.getValue();
        this.app.show("ingame", Map.of("playerAmount", playerAmount));
    }

}
