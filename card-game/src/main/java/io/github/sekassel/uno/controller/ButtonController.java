package io.github.sekassel.uno.controller;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
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
        System.out.println(this + " created.");
    }


    @onInit
    public void initSout() {
        System.out.println(this + " initialized.");
    }

    @onRender()
    public void renderSout() {
        System.out.println(this + " rendered.");
    }

    @onDestroy
    public void destroySout() {
        System.out.println(this + " destroyed.");
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
