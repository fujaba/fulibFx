package de.uniks.ludo.controller;

import de.uniks.ludo.App;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import org.fulib.fx.annotation.controller.Controller;

import javax.inject.Inject;
import java.util.Map;

@Controller
public class SetupController {

    @FXML
    private Slider playerAmountSlider;

    @Inject
    App app;

    @Inject
    public SetupController() {
    }

    @FXML
    public void onPlayClick() {
        int playerAmount = (int) this.playerAmountSlider.getValue();
        this.app.show("ingame", Map.of("playerAmount", playerAmount));
    }

}
