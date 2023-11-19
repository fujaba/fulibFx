package io.github.sekassel.person.controller;

import io.github.sekassel.person.backend.Person;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Controller(view = "view/sub/person.fxml")
public class PersonController extends VBox {

    private ObjectProperty<Person> person;

    @Inject
    public PersonController() {
        super();
    }

    public void setPerson(ObjectProperty<Person> person) {
        this.person = person;
    }

    public ObjectProperty<Person> getPerson() {
        return person;
    }






}
