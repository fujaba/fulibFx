package org.fulib.fx.app.controller.subcomponent.basic;

import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import javafx.fxml.FXML;

import javax.inject.Inject;

@Controller(view = "WithSubComponent.fxml")
public class SubComponentController {

    @SubComponent
    @Inject
    @FXML
    ButtonSubComponent buttonSubComponent;

    @Inject
    public SubComponentController() {
    }

}
