package org.fulib.fx.mocking.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.mocking.service.MyService;
import org.fulib.fx.controller.Subscriber;

import javax.inject.Inject;

@Controller
public class MyMainController {

    @Inject
    MyService service;

    @Inject
    Subscriber subscriber;

    @FXML
    public VBox render;

    @SubComponent
    @Inject
    MySubComponent mySubComponent;

    @Inject
    public MyMainController() {
    }

    @onRender
    public void onRender() {
        subscriber.subscribe(service.getObservable(), string -> render.getChildren().add(new Label(string)));
        render.getChildren().add(mySubComponent);
    }

}
