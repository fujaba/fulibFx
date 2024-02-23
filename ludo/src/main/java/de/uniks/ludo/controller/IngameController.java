package de.uniks.ludo.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.fulib.fx.annotation.controller.Controller;

import javax.inject.Inject;

@Controller
public class IngameController {

    @FXML
    private AnchorPane board;

    @Inject
    public IngameController() {
    }

}
