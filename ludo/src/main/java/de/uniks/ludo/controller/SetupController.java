package de.uniks.ludo.controller;

import de.uniks.ludo.App;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.Resource;
import org.fulib.fx.annotation.controller.Title;

import javax.inject.Inject;
import java.util.Map;
import java.util.ResourceBundle;

@Title("%setup.title")
@Controller
public class SetupController {

    @FXML
    public Button startButton;
    @FXML
    private Slider playerAmountSlider;

    @Resource
    @Inject
    ResourceBundle bundle;

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
