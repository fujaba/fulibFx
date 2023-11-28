package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import javafx.scene.Cursor;
import javafx.scene.control.Button;

import javax.inject.Inject;

@Controller
public class CustomButton extends Button {

    @Inject
    public CustomButton() {
        super();
        this.setCursor(Cursor.CROSSHAIR);
    }


    @ControllerEvent.onInit
    public void onInit() {
    }

    @ControllerEvent.onRender
    public void onRender() {
        this.setText("Dieser Button wurde gerendert.");
    }
}
