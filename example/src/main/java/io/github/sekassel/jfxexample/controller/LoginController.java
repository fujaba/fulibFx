package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxexample.ExampleApp;
import io.github.sekassel.jfxframework.controller.Controller;
import io.github.sekassel.jfxframework.controller.ControllerEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.Map;

@Controller(path = "view/login/login.fxml", route = "/menu/login")
public class LoginController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private Button loginButton;


    @ControllerEvent.onInit()
    public void init() {
        System.out.println("LoginController.onInit()");
    }

    @ControllerEvent.onRender()
    public void setupButton() {
        loginButton.disableProperty().bind(usernameTextField.textProperty().isEmpty());
    }

    @FXML
    public void buttonClick() {
        ExampleApp.instance.show("/menu/main", Map.of("username", usernameTextField.getText()));
    }

}
