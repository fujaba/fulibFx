package io.github.sekassel.person.routes;

import io.github.sekassel.jfxframework.controller.annotation.Providing;
import io.github.sekassel.jfxframework.controller.annotation.Route;
import io.github.sekassel.person.controller.ConfirmController;
import io.github.sekassel.person.controller.PersonController;
import io.github.sekassel.person.controller.PersonDisplayController;
import io.github.sekassel.person.controller.PersonListController;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Routing {

    @Route(route = "")
    @Inject
    public Provider<PersonListController> personList;

    @Providing
    @Inject
    public Provider<PersonController> personProvider;

    @Providing
    @Inject
    public Provider<PersonDisplayController> personDisplayProvider;

    @Providing
    @Inject
    public Provider<ConfirmController> confirmProvider;

    @Inject
    public Routing() {
    }

}
