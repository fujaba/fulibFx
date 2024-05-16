package org.fulib.fx.app.controller;

import org.fulib.fx.annotation.controller.Component;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.param.Param;

import javax.inject.Inject;

@Component
public class ModalComponent extends VBox {

    @Param("key")
    String value;

    @Inject
    public ModalComponent() {
        this.getChildren().add(new Label("Modal Component"));
    }

    public String getValue() {
        return this.value;
    }

}
