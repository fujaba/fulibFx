package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxexample.ExampleApp;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import javafx.fxml.FXML;

import javax.inject.Inject;
import java.util.Map;

@Controller
public class BackController {

    @FXML
    public void back() {
        ExampleApp.instance.show("..", Map.of());
    }

    @Inject
    public BackController() {
    }

}
