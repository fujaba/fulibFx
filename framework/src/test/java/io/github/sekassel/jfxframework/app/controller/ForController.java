package io.github.sekassel.jfxframework.app.controller;

import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.controller.SubComponent;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import io.github.sekassel.jfxframework.annotation.param.Param;
import io.github.sekassel.jfxframework.app.controller.sub.ButtonSubComponent;
import io.github.sekassel.jfxframework.constructs.For;
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
