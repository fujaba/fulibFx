package io.github.sekassel.jfxexample.routes;

import io.github.sekassel.jfxexample.controller.BackController;
import io.github.sekassel.jfxexample.controller.LoginController;
import io.github.sekassel.jfxexample.controller.MainController;
import io.github.sekassel.jfxframework.controller.annotation.Route;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Routing {

    @Route(route = "")
    @Inject
    public Provider<LoginController> login;

    @Route(route = "/mainmenu")
    @Inject
    public Provider<MainController> main;

    @Route(route = "/mainmenu/back")
    @Inject
    public Provider<BackController> back;

    @Inject
    public Routing() {
    }

}
