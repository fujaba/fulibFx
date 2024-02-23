package de.uniks.ludo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import org.fulib.fx.annotation.controller.Controller;

import javax.inject.Inject;

@Controller
public class SetupController {

    @FXML
    private Slider playerAmountSlider;

    @Inject
    public SetupController() {
    }

    @FXML
    public void onPlayClick() {

    }

}
