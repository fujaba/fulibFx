package io.github.sekassel.jfxexample.controller2;

import io.github.sekassel.jfxexample.ExampleApp;
import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.Param;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.util.Map;

@Controller(id = "mainmenu")
public class MainController {

    @FXML
    public Label welcomeLabel;

    @FXML
    public Button back;

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

    public void back(ActionEvent actionEvent) {
        ExampleApp.instance.show("..", Map.of());
    }
}
