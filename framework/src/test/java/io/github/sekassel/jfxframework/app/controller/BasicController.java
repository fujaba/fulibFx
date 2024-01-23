package io.github.sekassel.jfxframework.app.controller;

import io.github.sekassel.jfxframework.annotation.controller.Controller;

import javax.inject.Inject;

@Controller // No view specified, so the view is the same as the controller name (basic.fxml)
public class BasicController {

    @Inject
    public BasicController() {
        // The FXML file contains a label with the text "Basic Controller"
    }
}
