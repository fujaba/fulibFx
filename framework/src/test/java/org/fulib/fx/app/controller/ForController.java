package org.fulib.fx.app.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.app.controller.subcomponent.basic.ButtonSubComponent;
import org.fulib.fx.constructs.forloop.FxFor;
import org.fulib.fx.controller.Subscriber;

import javax.inject.Inject;
import javax.inject.Provider;

@Controller(view = "#render")
public class ForController {

    @Inject
    Provider<ButtonSubComponent> subComponentProvider;

    @Inject
    FxFor fxFor;

    @Inject
    Subscriber subscriber;

    private VBox container;

    @Inject
    public ForController() {
    }

    VBox render() {
        this.container = new VBox();
        this.container.setId("container");

        return new VBox(container);
    }

    @OnRender
    public void onRender(@Param("list") ObservableList<String> list) {
        subscriber.subscribe(fxFor.of(container, list, subComponentProvider, Labeled::setText).disposable());
    }

    @OnDestroy
    public void onDestroy() {
        subscriber.dispose();
    }

}
