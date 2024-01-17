package io.github.sekassel.jfxframework.app.controller;

import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.controller.SubComponent;
import io.github.sekassel.jfxframework.app.controller.sub.ButtonSubComponent;
import javafx.fxml.FXML;

import javax.inject.Inject;

@Controller(view = "withsubcomponent.fxml")
public class SubComponentController {

    @SubComponent
    @Inject
    @FXML
    ButtonSubComponent buttonSubComponent;

    @Inject
    public SubComponentController() {
    }

}
