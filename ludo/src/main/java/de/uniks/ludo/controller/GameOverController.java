package de.uniks.ludo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.fulib.fx.annotation.controller.Controller;

import javax.inject.Inject;

@Controller
public class GameOverController {

    @FXML
    private Label playerWonLabel;

    @Inject
    public GameOverController() {
    }

}
