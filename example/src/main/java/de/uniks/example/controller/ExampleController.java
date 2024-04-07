package de.uniks.example.controller;

import de.uniks.example.controller.sub.ExampleSubComponent;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.controller.Title;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onKey;
import org.fulib.fx.annotation.event.onRender;

import javax.inject.Inject;

@Title("%example.title")
@Controller(view = "Example.fxml")
public class ExampleController extends BaseController {

    @FXML
    public VBox vBox;

    @Inject
    @SubComponent
    public ExampleSubComponent exampleSubComponent;

    @Inject
    public ExampleController() {
    }

    @onInit
    public void onInit() {
        subscriber.subscribe(() -> System.out.println("Destroyed!"));
    }

    @onRender
    public void onRender() {
        exampleSubComponent.setText("Hello World!");
        vBox.getChildren().add(exampleSubComponent);
    }

    @onDestroy
    public void onDestroy() {
        this.subscriber.dispose();
    }

    @onKey(code = KeyCode.F5, shift = true)
    public void reload() {
        app.refresh();
    }

}
