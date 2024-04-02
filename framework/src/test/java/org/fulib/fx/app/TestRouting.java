package org.fulib.fx.app;

import org.fulib.fx.annotation.Route;
import org.fulib.fx.app.controller.*;
import org.fulib.fx.app.controller.subcomponent.basic.SubComponentController;
import org.fulib.fx.app.controller.types.*;
import org.fulib.fx.app.controller.subcomponent.order.MainController;

import javax.inject.Inject;
import javax.inject.Provider;

public class TestRouting {

    @Inject
    @Route("component/root")
    Provider<RootComponent> rootComponentProvider;

    @Inject
    @Route("component/basic")
    Provider<BasicComponent> basicComponentProvider;

    @Inject
    @Route("controller/basic")
    Provider<BasicController> basicControllerProvider;

    @Inject
    @Route("controller/view")
    Provider<FXMLController> viewControllerProvider;

    @Inject
    @Route("controller/method")
    Provider<MethodController> methodControllerProvider;

    @Inject
    @Route("controller/withsubcomponent")
    Provider<SubComponentController> subComponentControllerProvider;

    @Inject
    @Route("controller/for")
    Provider<ForController> forControllerProvider;

    @Inject
    @Route("ordertest/main")
    Provider<MainController> mainControllerProvider;

    @Inject
    public TestRouting() {
    }

}
