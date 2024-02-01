package org.fulib.fx.app.controller;

import org.fulib.fx.annotation.controller.Component;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Component
public class BasicComponent extends VBox {

    @Inject
    public BasicComponent() {
        this.getChildren().add(new Label("Basic Component"));
    }
}
