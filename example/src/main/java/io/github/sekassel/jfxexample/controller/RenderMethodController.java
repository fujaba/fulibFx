package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxframework.controller.annotation.Controller;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Controller(view = "#render")
public class RenderMethodController {

    public VBox render() {
        return new VBox(new Label("Render method called"));
    }

    @Inject
    public RenderMethodController() {
    }

}
