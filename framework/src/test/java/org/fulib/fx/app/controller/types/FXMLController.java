package org.fulib.fx.app.controller.types;

import org.fulib.fx.annotation.controller.Controller;

import javax.inject.Inject;

@Controller(view = "../view/View.fxml") // Path traversal
public class FXMLController {

    @Inject
    public FXMLController() {
        // The FXML file contains a label with the text "View Controller"
    }
}
