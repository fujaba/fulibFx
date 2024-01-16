package io.github.sekassel.jfxframework.app.controller;

import io.github.sekassel.jfxframework.annotation.controller.Component;
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
