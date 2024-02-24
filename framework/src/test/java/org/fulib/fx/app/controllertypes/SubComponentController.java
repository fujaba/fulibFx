package org.fulib.fx.app.controllertypes;

import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.app.controllertypes.sub.ButtonSubComponent;
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
