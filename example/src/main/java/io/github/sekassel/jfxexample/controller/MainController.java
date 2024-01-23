package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxexample.ExampleApp;
import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import io.github.sekassel.jfxframework.annotation.param.Param;
import io.github.sekassel.jfxframework.annotation.param.Params;
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

    @onRender()
    public void setText(@Param(value = "username") String username, @Param(value = "password") String password) {
        welcomeLabel.setText("Welcome " + username + ", your password is " + password);
    }

    @onRender()
    public void listParameters(@Params Map<String, Object> params) {
        params.forEach((key, value) -> ((Pane) welcomeLabel.getParent()).getChildren().add(new Label(key + ": " + value)));
    }

    @FXML
    public void continueButton() {
        ExampleApp.instance.show("back", Map.of("key", "value"));
    }
}
