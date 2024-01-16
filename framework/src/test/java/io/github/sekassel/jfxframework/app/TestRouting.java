package io.github.sekassel.jfxframework.app;

import io.github.sekassel.jfxframework.annotation.Route;
import io.github.sekassel.jfxframework.app.controller.*;

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
    Provider<ViewController> viewControllerProvider;

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
    public TestRouting() {
    }

}
