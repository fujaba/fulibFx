package org.fulib.fx.app.controller;

import org.fulib.fx.annotation.controller.Component;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Component(view = "root.fxml")
public class RootComponent extends VBox {

    @FXML
    public Label rootLabel;

    @Inject
    public RootComponent() {
        // The FXML file contains a label with the text "Root Component"
    }
}
