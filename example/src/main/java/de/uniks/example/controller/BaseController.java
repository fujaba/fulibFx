package de.uniks.example.controller;

import de.uniks.example.ExampleApp;
import org.fulib.fx.annotation.controller.Resource;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.controller.Subscriber;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class BaseController {

    @Inject
    public BaseController() {
    }

    @Inject
    @Resource
    ResourceBundle bundle;

    @Inject
    ExampleApp app;

    @Inject
    Subscriber subscriber;

    @onInit
    public void init() {
        System.out.println("Initialized!");
    }

}
