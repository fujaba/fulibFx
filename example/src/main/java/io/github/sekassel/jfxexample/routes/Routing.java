package io.github.sekassel.jfxexample.routes;

import io.github.sekassel.jfxexample.controller.LoginController;
import io.github.sekassel.jfxexample.controller2.MainController;
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

    @Inject
    public Routing() {
    }

}
