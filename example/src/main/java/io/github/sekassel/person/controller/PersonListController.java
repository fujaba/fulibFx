package io.github.sekassel.person.controller;

import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.person.backend.Person;
import javafx.application.Platform;
import javafx.beans.property.*;
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

    ObservableList<Person> personList = FXCollections.observableArrayList(
            new Person("", "Jocky", "Lowell", 42),
            new Person("", "Jason", "Howdey", 34)
    );

    public ObservableList<Person> getPersonList()
    {
        return personList;
    }

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
                Platform.runLater(() -> personList.add(new Person("", "Person" + i.incrementAndGet(), "Mayer", i.get())));
            }
        }).start();
    }


}
