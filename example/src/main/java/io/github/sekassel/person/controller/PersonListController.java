package io.github.sekassel.person.controller;

import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.person.backend.Person;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

@Controller(view = "view/persons.fxml")
public class PersonListController {

    @FXML
    public Label label;

    @FXML
    public HBox controller;

    @FXML
    ObservableList<Person> persons = FXCollections.observableArrayList(
            new Person("", "Jocky", "Lowell", 42),
            new Person("", "Jason", "Howdey", 34)
    );

    @Inject
    public PersonListController() {
    }


    @ControllerEvent.onRender
    public void onRender() {
        // Simulate adding new persons
        new Thread(() -> {
            AtomicInteger i = new AtomicInteger();
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> persons.add(new Person("A", "A" + i.incrementAndGet(), "C", 12)));
            }
        }).start();
    }


}
