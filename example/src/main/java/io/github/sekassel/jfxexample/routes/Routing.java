package io.github.sekassel.jfxexample.routes;

import io.github.sekassel.jfxexample.controller.*;
import io.github.sekassel.jfxframework.controller.annotation.Providing;
import io.github.sekassel.jfxframework.controller.annotation.Route;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Routing {

    @Route(value = "")
    @Inject
    public Provider<LoginController> login;

    @Route(value = "/mainmenu")
    @Inject
    public Provider<MainController> main;

    @Route(value = "/mainmenu/back")
    @Inject
    public Provider<BackController> back;

    @Route(value = "/mainmenu/back/rendermethod")
    @Inject
    public Provider<RenderMethodController> rendermethod;

    @Route(value = "/history")
    @Inject
    public Provider<HistoryController> history;

    @Providing
    @Inject
    public Provider<CustomButton> buttonProvider;

    @Inject
    public Routing() {
    }

}
