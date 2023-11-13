package io.github.sekassel.todo.controller;

import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.Param;
import io.github.sekassel.todo.Todo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;

@Controller(id = "mainmenu")
public class MainController {
    public final ObservableList<Todo> todos = FXCollections.observableArrayList(
        new Todo("Do stuff", "Very important stuff"),
        new Todo("Big things", ""),
        new Todo("More stuff", "")
    );

    @Inject
    public MainController() {
    }

    @ControllerEvent.onInit()
    public void init(@Param(name = "username") String user) {
        System.out.println("MainController.onInit() " + user);
    }
}
