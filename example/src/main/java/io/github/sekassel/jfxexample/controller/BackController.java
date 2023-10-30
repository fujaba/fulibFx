package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxexample.ExampleApp;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.Map;

@Controller
public class BackController extends VBox {

    @Inject
    public BackController() {
        Button button = new Button("Back");
        button.setOnAction(event -> back());

        Button button1 = new Button("Continue");
        button1.setOnAction(event -> ExampleApp.instance.show("rendermethod", Map.of()));

        this.getChildren().add(button);
        this.getChildren().add(button1);
    }

    @FXML
    public void back() {
        ExampleApp.instance.show("..", Map.of());
    }

}
