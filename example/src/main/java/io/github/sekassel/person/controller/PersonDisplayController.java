package io.github.sekassel.person.controller;

import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import io.github.sekassel.person.backend.Person;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.List;

@Controller(view = "view/sub/person-display.fxml")
public class PersonDisplayController extends VBox {

    @FXML
    public TextField age;
    @FXML
    public TextField lastName;
    @FXML
    public TextField firstName;
    @FXML
    public TextField imageLink;
    @FXML
    public ImageView image;
    @FXML
    public Button editButton;
    @FXML
    public Button saveAsNewButton;

    @Inject
    public PersonDisplayController() {
        System.out.println("\tPersonDisplayController.constructor");
    }

    private List<Person> personList;

    private Person current;

    public void setPerson(Person current) {
        this.current = current;
    }

    @ControllerEvent.onInit
    public void init() {
        System.out.println("\tPersonDisplayController.onInit");
    }

    @ControllerEvent.onRender
    public void render() {
        System.out.println("\tPersonDisplayController.onRender");
    }

    @ControllerEvent.onRender
    public void refresh() {
        if (current == null) return;
        firstName.setText(current.firstName());
        lastName.setText(current.lastName());
        age.setText(String.valueOf(current.age()));
        imageLink.setText(current.image());
        image.setImage(new Image(current.image()));
    }

    @FXML
    public void toggleEdit() {
        if (editButton.getText().equals("Edit/Add")) {
            this.enableEdit();
        } else {
            this.disableEdit();

            Person newPerson = new Person(
                    imageLink.getText(),
                    firstName.getText(),
                    lastName.getText(),
                    Integer.parseInt(age.getText())
            );

            this.updateInList(newPerson);
            this.setPerson(newPerson);
        }
    }

    @FXML
    public void saveAsNew() {
        Person newPerson = new Person(
                imageLink.getText(),
                firstName.getText(),
                lastName.getText(),
                Integer.parseInt(age.getText())
        );

        if (personList.contains(newPerson))
            return;

        this.personList.add(newPerson);
        this.setPerson(newPerson);
    }

    private void updateInList(Person newPerson) {
        if (this.current != null) this.personList.remove(current);
        this.personList.add(newPerson);
    }

    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    private void enableEdit() {
        editButton.setText("Save");
        saveAsNewButton.setVisible(true);
        saveAsNewButton.setDisable(false);
        firstName.setEditable(true);
        lastName.setEditable(true);
        age.setEditable(true);
        imageLink.setEditable(true);
    }

    private void disableEdit() {
        editButton.setText("Edit/Add");
        firstName.setEditable(false);
        lastName.setEditable(false);
        age.setEditable(false);
        saveAsNewButton.setVisible(true);
        saveAsNewButton.setDisable(false);
    }
}
