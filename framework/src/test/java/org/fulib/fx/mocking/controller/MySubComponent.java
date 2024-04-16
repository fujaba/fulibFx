package org.fulib.fx.mocking.controller;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.OnRender;

import javax.inject.Inject;

@Component
public class MySubComponent extends VBox {

    @Inject
    public MySubComponent() {
    }

    @OnRender
    public void onRender() {
        this.getChildren().add(new Label("This is a subcomponent string."));
    }

}
