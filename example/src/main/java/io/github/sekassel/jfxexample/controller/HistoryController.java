package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxexample.ExampleApp;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Param;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.Map;

@Controller
public class HistoryController extends VBox {

    public static int counter = 0;


    Label label = new Label("History: %s");

    @Inject
    public HistoryController() {
        super();


        this.getChildren().add(label);

        Button button = new Button("Back");
        button.setOnAction(event -> ExampleApp.instance.back());
        this.getChildren().add(button);

        Button forwardButton = new Button("Forward");
        forwardButton.setOnAction(event -> ExampleApp.instance.forward());
        this.getChildren().add(forwardButton);

        Button showNewButton = new Button("Show new");
        showNewButton.setOnAction(event -> ExampleApp.instance.show("/history", Map.of("counter", ++counter)));
        this.getChildren().add(showNewButton);
    }

    @ControllerEvent.onRender
    public void onRender(@Param(name = "counter") Integer counter) {
        label.setText(String.format("History: %s", counter));
    }
}
