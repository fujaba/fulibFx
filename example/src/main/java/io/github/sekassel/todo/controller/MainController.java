package io.github.sekassel.todo.controller;

import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.Param;

import javax.inject.Inject;

@Controller(id = "mainmenu")
public class MainController {

    @Inject
    public MainController() {
    }

    @ControllerEvent.onInit()
    public void init(@Param(name = "username") String user) {
        System.out.println("MainController.onInit() " + user);
    }
}
