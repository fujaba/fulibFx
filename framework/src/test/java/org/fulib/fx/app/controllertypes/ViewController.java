package org.fulib.fx.app.controllertypes;

import org.fulib.fx.annotation.controller.Controller;

import javax.inject.Inject;

@Controller(view = "../view/view.fxml")
public class ViewController {

    @Inject
    public ViewController() {
        // The FXML file contains a label with the text "View Controller"
    }
}
