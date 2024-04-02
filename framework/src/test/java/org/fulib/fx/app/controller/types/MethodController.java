package org.fulib.fx.app.controller.types;

import org.fulib.fx.annotation.controller.Controller;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Controller(view = "#render")
public class MethodController {

    @Inject
    public MethodController() {
        // The view is rendered by the render() method
    }

    VBox render() {
        return new VBox(new Label("Method Controller"));
    }
}
