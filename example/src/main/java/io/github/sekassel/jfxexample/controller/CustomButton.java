package io.github.sekassel.jfxexample.controller;

import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.event.onInit;
import io.github.sekassel.jfxframework.annotation.event.onRender;
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


    @onInit
    public void onInit() {
    }

    @onRender
    public void onRender() {
        this.setText("Dieser Button wurde gerendert.");
    }
}
