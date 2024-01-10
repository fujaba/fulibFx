package io.github.sekassel.uno.controller;

import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

@Controller(view = "view/sub/buttons.fxml")
public class ButtonController extends HBox {

    public void setParentController(IngameController parentController) {
        this.parentController = parentController;
    }

    private IngameController parentController;

    @Inject
    public ButtonController() {
        System.out.println("ButtonController.constructor");
    }

    @ControllerEvent.onInit
    public void init() {
        System.out.println("ButtonController.onInit");
    }

    @ControllerEvent.onRender
    public void render() {
        System.out.println("ButtonController.onRender");
    }

    @ControllerEvent.onDestroy
    public void destroy() {
        System.out.println("ButtonController.onDestroy");
    }



    /**
     * The method triggered by the wild card buttons.
     * Different behaviour is defined by the id of the clicked button.
     * Chooses the card color and plays the wild card as a colored card.
     */
    @FXML
    public void onWildPressed(ActionEvent event) {
        this.parentController.onWildPressed(event);
    }
}
