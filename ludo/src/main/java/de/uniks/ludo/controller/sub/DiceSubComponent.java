package de.uniks.ludo.controller.sub;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Component;

import javax.inject.Inject;

@Component
public class DiceSubComponent extends VBox {

    @FXML
    private Label eyes;

    @Inject
    public DiceSubComponent() {
        super();
    }

}
