package io.github.sekassel.person.controller;

import io.github.sekassel.jfxframework.constructs.For;
import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.person.backend.Person;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

@Controller(view = "view/persons.fxml")
public class PersonListController {

    @FXML
    public Label label;

    @FXML
    public VBox nodeList;

    @FXML
    public VBox controllerList;

    ObservableList<Person> personList = FXCollections.observableArrayList(
            new Person("https://cdn-icons-png.flaticon.com/512/2815/2815428.png", "Jocky", "Lowell", 42),
            new Person("https://cdn-icons-png.flaticon.com/512/2815/2815428.png", "Jason", "Howdey", 34)
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

        Button button = new Button("Text");
        button.setOnMouseClicked(event -> System.out.println("Hello"));

        For.create(nodeList, personList, new VBox(button, new Label("Label")));
        For.create(controllerList, personList, PersonController.class);

        new Thread(() -> {
            AtomicInteger i = new AtomicInteger();
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    System.out.println(i.get());
                    if (i.get() % 2 == 0) {
                        personList.remove(0);
                    }
                    else
                        personList.add(new Person("https://cdn-icons-png.flaticon.com/512/2815/2815428.png", "Person" + i.get(), "Mayer", i.get()));
                });
                i.incrementAndGet();
            }
        }).start();
    }


}
