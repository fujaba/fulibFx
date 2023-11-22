package io.github.sekassel.person.controller;

import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.person.backend.Person;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Controller(view = "view/sub/person.fxml")
public class PersonController extends VBox {

    @FXML
    public Label lastName;
    @FXML
    public Label age;
    @FXML
    public ImageView image;
    @FXML
    public Label firstName;

    private Person person;

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

    @ControllerEvent.onRender
    public void onRender() {
        firstName.setText(person.firstName());
        lastName.setText(person.lastName());
        age.setText(String.valueOf(person.age()));
        image.setImage(new Image(person.image()));

    }


}
