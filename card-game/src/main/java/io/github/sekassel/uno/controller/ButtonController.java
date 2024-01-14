package io.github.sekassel.uno.controller;

import io.github.sekassel.jfxframework.annotation.controller.Component;
import io.github.sekassel.jfxframework.annotation.event.onDestroy;
import io.github.sekassel.jfxframework.annotation.event.onInit;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

@Component(view = "view/sub/buttons.fxml")
public class ButtonController extends HBox {

    public void setParentController(IngameController parentController) {
        this.parentController = parentController;
    }

    private IngameController parentController;

    @Inject
    public ButtonController() {
        System.out.println("ButtonController.constructor");
    }

    @onInit
    public void init() {
        System.out.println("ButtonController.onInit");
    }

    @onRender
    public void render() {
        System.out.println("ButtonController.onRender");
    }

    @onDestroy
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