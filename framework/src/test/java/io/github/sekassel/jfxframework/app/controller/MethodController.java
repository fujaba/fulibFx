package io.github.sekassel.jfxframework.app.controller;

import io.github.sekassel.jfxframework.annotation.controller.Controller;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Controller(view = "#render")
public class MethodController {

    @Inject
    public MethodController() {
        // The view is rendered by the render() method
    }

    private VBox render() {
        return new VBox(new Label("Method Controller"));
    }
}
