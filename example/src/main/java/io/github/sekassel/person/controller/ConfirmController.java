package io.github.sekassel.person.controller;

import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Param;
import io.github.sekassel.person.backend.Person;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Controller
public class ConfirmController extends VBox {

    private Runnable onConfirm;
    private Runnable onCancel;

    private final Label label;

    @Inject
    public ConfirmController() {
        // A very ugly way of setting up a controller without an FXML
        System.out.println("ConfirmController.constructor");
        this.label = new Label();
        this.getChildren().add(this.label);

        Button button = new Button("Confirm");
        button.setOnAction(e -> confirm());
        this.getChildren().add(new HBox(button));

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> cancel());
        this.getChildren().add(new HBox(cancelButton));

    }

    @ControllerEvent.onRender
    private void onRender(@Param(name = "person") Person person) {
        System.out.println("ConfirmController.onRender");
        this.label.setText("Do you want to delete %s?".formatted(person.firstName() + " " + person.lastName()));
    }

    @ControllerEvent.onInit
    private void onInit(@Param(name = "person") Person person) {
        System.out.println("ConfirmController.onInit");
    }

    @ControllerEvent.onDestroy
    private void onDestroy() {
        System.out.println("ConfirmController.onDestroy");
    }

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void confirm() {
        if (onConfirm != null) onConfirm.run();
    }

    public void cancel() {
        if (onCancel != null) onCancel.run();
    }

}
