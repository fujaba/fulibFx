package org.fulib.fx.app.controller.types;

import org.fulib.fx.annotation.controller.Controller;

import javax.inject.Inject;

@Controller // No view specified, so the view is the same as the controller name (Basic.fxml)
public class BasicController {

    @Inject
    public BasicController() {
        // The FXML file contains a label with the text "Basic Controller"
    }
}
