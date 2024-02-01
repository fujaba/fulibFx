package org.fulib.fx.app.controller;

import org.fulib.fx.annotation.controller.Controller;

import javax.inject.Inject;

@Controller // No view specified, so the view is the same as the controller name (basic.fxml)
public class BasicController {

    @Inject
    public BasicController() {
        // The FXML file contains a label with the text "Basic Controller"
    }
}
