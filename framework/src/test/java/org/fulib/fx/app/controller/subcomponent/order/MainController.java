package org.fulib.fx.app.controller.subcomponent.order;

import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnInit;
import org.fulib.fx.annotation.event.OnRender;
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

    @OnInit()
    public void init(@Param("initList") List<String> initList) {
        initList.add("main");
    }

    @OnRender()
    public void render(@Param("renderList") List<String> renderList) {
        renderList.add("main");
    }

    @OnDestroy()
    public void destroy() {
        destroyList.add("main");
    }

}
