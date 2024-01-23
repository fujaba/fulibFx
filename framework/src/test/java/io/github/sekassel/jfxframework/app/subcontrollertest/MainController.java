package io.github.sekassel.jfxframework.app.subcontrollertest;

import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.controller.SubComponent;
import io.github.sekassel.jfxframework.annotation.event.onDestroy;
import io.github.sekassel.jfxframework.annotation.event.onInit;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import io.github.sekassel.jfxframework.annotation.param.Param;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.List;

@Controller(view = "#view")
public class MainController {

    @Param("destroyList")
    List<String> destroyList;

    @Inject
    public MainController() {
    }

    @Inject
    @SubComponent
    SubController subController;

    public VBox view() {
        return new VBox();
    }


    @onInit()
    public void init(@Param("initList") List<String> initList) {
        initList.add("main");
    }

    @onRender()
    public void render(@Param("renderList") List<String> renderList) {
        renderList.add("main");
    }

    @onDestroy()
    public void destroy() {
        destroyList.add("main");
    }

}
