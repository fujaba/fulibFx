package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxexample.ExampleApp;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Param;
import io.github.sekassel.jfxframework.controller.annotation.Params;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.util.Map;

@Controller()
public class MainController {

    @FXML
    public Label welcomeLabel;

    @FXML
    public Button continueButton;

    @Inject
    public MainController() {
    }

    @ControllerEvent.onRender()
    public void setText(@Param(value = "username") String username, @Param(value = "password") String password) {
        welcomeLabel.setText("Welcome " + username + ", your password is " + password);
    }

    @ControllerEvent.onRender()
    public void listParameters(@Params Map<String, Object> params) {
        params.forEach((key, value) -> ((Pane) welcomeLabel.getParent()).getChildren().add(new Label(key + ": " + value)));
    }

    @FXML
    public void continueButton() {
        ExampleApp.instance.show("back", Map.of("key", "value"));
    }
}
