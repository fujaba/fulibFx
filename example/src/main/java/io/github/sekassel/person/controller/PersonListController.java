package io.github.sekassel.person.controller;

import io.github.sekassel.jfxframework.annotation.event.onDestroy;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import io.github.sekassel.jfxframework.constructs.For;
import io.github.sekassel.jfxframework.controller.Subscriber;
import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.event.onInit;
import io.github.sekassel.person.PersonApp;
import io.github.sekassel.person.backend.Person;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

@Controller(view = "view/persons.fxml")
public class PersonListController {


    @Inject
    PersonApp app;

    @FXML
    public VBox friendMenu;
    @FXML
    public Button refreshButton;
    @FXML
    public Label personListLabel;
    @FXML
    public VBox friendEditor;
    @FXML
    public Label currentLabel;
    @FXML
    public VBox friendList;

    @Inject
    Subscriber subscriber;

    @FXML
    public PersonDisplayController currentFriend;

    ObservableList<Person> personList = FXCollections.observableArrayList(
            new Person("https://cdn-icons-png.flaticon.com/512/2815/2815428.png", "Max", "Mustermann", 32)
    );

    @Inject
    public PersonListController() {
        System.out.println("PersonListController.constructor");
    }

    public ObservableList<Person> getPersonList() {
        return personList;
    }

    @onInit
    public void onInit() {
        System.out.println("PersonListController.onInit");
    }

    @onRender
    public void onRender() {
        System.out.println("PersonListController.onRender");

        this.currentFriend.setPersonList(personList);
        if (!personList.isEmpty()) {
            this.currentFriend.setPerson(personList.get(0));
            this.currentFriend.refresh();
        }

        Disposable disposable = For.controller(friendList, personList, PersonController.class, (personController, person) -> {

            System.out.println("PersonController.beforeInit");
            personController.setPerson(person);
            personController.setList(personList);
            personController.setOnMouseClicked(event -> {
                currentFriend.setPerson(person);
                currentFriend.refresh();
            });
        }).disposable();

        subscriber.addDestroyable(disposable);
    }

    @onDestroy
    public void onDestroy() {
        System.out.println("PersonListController.onDestroy");
    }

    public void refresh(ActionEvent actionEvent) {
        PersonApp.instance.refresh();
        System.out.println("Reload");
    }
}
