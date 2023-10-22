package io.github.sekassel.jfxexample.controller2;

import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Param;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javax.inject.Inject;

@Controller(id = "mainmenu")
public class MainController {

    @FXML
    public Label welcomeLabel;

    @Inject
    public MainController() {
    }

    @ControllerEvent.onInit()
    public void init(@Param(name = "username") String user) {
        System.out.println("MainController.onInit() " + user);
    }

    @ControllerEvent.onRender()
    public void setText(@Param(name = "username") String username, @Param(name = "password") String password) {
        welcomeLabel.setText("Welcome " + username + ", your password is " + password);
    }
}
