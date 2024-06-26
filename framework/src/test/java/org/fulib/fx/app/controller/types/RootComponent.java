package org.fulib.fx.app.controller.types;

import org.fulib.fx.annotation.controller.Component;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Component(view = "Root.fxml")
public class RootComponent extends VBox {

    @FXML
    public Label rootLabel;

    @Inject
    public RootComponent() {
        // The FXML file contains a label with the text "Root Component"
    }
}
