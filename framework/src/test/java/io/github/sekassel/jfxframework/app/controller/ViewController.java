package io.github.sekassel.jfxframework.app.controller;

import io.github.sekassel.jfxframework.annotation.controller.Controller;

import javax.inject.Inject;

@Controller(view = "../view/view.fxml")
public class ViewController {

    @Inject
    public ViewController() {
        // The FXML file contains a label with the text "View Controller"
    }
}
