package io.github.sekassel.person.controller;

import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.person.backend.Person;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import javax.inject.Inject;
import java.util.List;

@Controller(view = "view/sub/person.fxml")
public class PersonController extends HBox {

    @FXML
    public Label firstName;
    @FXML
    public Label lastName;
    @FXML
    public ImageView image;
    @FXML
    public Button deleteButton;

    private Person person;
    private List<Person> personList;

    @Inject
    public PersonController() {
        super();
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
    }

    @ControllerEvent.onRender
    public void onRender() {
        firstName.setText(person.firstName());
        lastName.setText(person.lastName());
        image.setImage(new Image(person.image()));
        deleteButton.setOnAction(event -> personList.remove(person));
        deleteButton.setOnMouseClicked(event -> personList.remove(person));
    }
}
