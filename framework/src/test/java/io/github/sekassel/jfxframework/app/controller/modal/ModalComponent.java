package io.github.sekassel.jfxframework.app.controller.modal;

import io.github.sekassel.jfxframework.annotation.controller.Component;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Component
public class ModalComponent extends VBox {

    @Inject
    public ModalComponent() {
        this.getChildren().add(new Label("Modal Component"));
    }

}
