package org.fulib.fx.app.subcontrollertest;

import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
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
