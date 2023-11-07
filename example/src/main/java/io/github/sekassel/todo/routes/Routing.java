package io.github.sekassel.todo.routes;

import io.github.sekassel.jfxframework.controller.annotation.Route;
import io.github.sekassel.todo.controller.MainController;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Routing {

    @Route(route = "")
    @Inject
    public Provider<MainController> main;

    @Inject
    public Routing() {
    }
}
