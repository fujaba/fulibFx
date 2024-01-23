package io.github.sekassel.jfxframework.app.subcontrollertest;

import io.github.sekassel.jfxframework.annotation.controller.Component;
import io.github.sekassel.jfxframework.annotation.event.onDestroy;
import io.github.sekassel.jfxframework.annotation.event.onInit;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import io.github.sekassel.jfxframework.annotation.param.Param;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.List;

@Component
public class SubSubController extends VBox {

    @Param("destroyList")
    List<String> destroyList;

    @Inject
    public SubSubController() {
    }

    @onInit()
    public void init(@Param("initList") List<String> initList) {
        initList.add("subsub");
    }

    @onRender()
    public void render(@Param("renderList") List<String> renderList) {
        renderList.add("subsub");
    }

    @onDestroy()
    public void destroy() {
        destroyList.add("subsub");
    }

}
