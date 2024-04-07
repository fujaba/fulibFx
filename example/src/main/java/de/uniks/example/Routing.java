package de.uniks.example;

import de.uniks.example.controller.ExampleController;
import org.fulib.fx.annotation.Route;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Routing {

    @Inject
    @Route("")
    public Provider<ExampleController> exampleController;

    @Inject
    public Routing() {
    }
}
