package io.github.sekassel.person.controller;

import io.github.sekassel.jfxframework.controller.Subscriber;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import io.github.sekassel.person.PersonApp;
import io.github.sekassel.person.backend.Person;
import io.github.sekassel.jfxframework.controller.Modals;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Controller(view = "view/sub/person.fxml")
public class PersonController extends HBox implements Subscriber {

    @FXML
    public Label firstName;
    @FXML
    public Label lastName;
    @FXML
    public ImageView image;
    @FXML
    public Button deleteButton;

    @Inject
    PersonApp app;

    private Person person;
    private List<Person> personList;

    @Inject
    public PersonController() {
        super();
        System.out.println("PersonController.constructor");
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setList(List<Person> personList) {
        this.personList = personList;
    }

    @ControllerEvent.onInit
    public void onInit() {
        System.out.println("PersonController.onInit");
        addDestroyable(() -> System.out.println("PersonController->Subscriber.onDestroy"));
    }

    @ControllerEvent.onRender
    public void onRender() {
        System.out.println("PersonController.onRender");
        firstName.setText(person.firstName());
        lastName.setText(person.lastName());
        image.setImage(new Image(person.image()));

        // Open a modal when the delete button is clicked
        deleteButton.setOnMouseClicked(event -> Modals.showModal(app.stage(), ConfirmController.class, (modalStage, controller) -> {
            controller.setOnConfirm(() -> {
                personList.remove(person);
                modalStage.close();
            });
            controller.setOnCancel(modalStage::close);
        }, Map.of("person", person), true));
    }

    @ControllerEvent.onDestroy
    public void onDestroy() {
        System.out.println("PersonController.onDestroy");
    }
}
