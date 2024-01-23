package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxexample.ExampleApp;
import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.event.onInit;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.util.Map;

@Controller(view = "view/login/login.fxml")
public class LoginController {

    @FXML
    public TextField otherTextField;
    @FXML
    private TextField usernameTextField;

    @FXML
    private Button loginButton;

    @Inject
    public LoginController() {
    }

    @onInit()
    public void init() {
    }

    @onRender()
    public void setupButton() {
        loginButton.disableProperty().bind(usernameTextField.textProperty().isEmpty());
    }

    @FXML
    public void buttonClick() {
        ExampleApp.instance.show("mainmenu", Map.of("username", usernameTextField.getText(), "example", 42));
    }

}
