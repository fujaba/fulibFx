package org.fulib.fx.app.controller;

import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.app.controller.sub.ButtonSubComponent;
import org.fulib.fx.constructs.For;
import javafx.collections.ObservableList;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import javax.inject.Provider;

@Controller(view = "#render")
public class ForController {

    @Inject
    @SubComponent
    Provider<ButtonSubComponent> subComponentProvider;

    private VBox container;

    @Inject
    public ForController() {
    }

    private VBox render() {
        this.container = new VBox();
        this.container.setId("container");

        return new VBox(container);
    }

    @onRender
    public void onRender(@Param("list") ObservableList<String> list) {
        For.of(container, list, subComponentProvider, Labeled::setText);
    }

}
