package org.fulib.fx.app.subcontrollertest;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.List;

@Component
public class SubController extends VBox {

    @Param("destroyList")
    List<String> destroyList;

    @Inject
    @SubComponent
    SubSubController subSubController;

    @Inject
    @SubComponent
    OtherSubSubController otherSubSubController;

    @Inject
    public SubController() {
    }

    @onInit()
    public void init(@Param("initList") List<String> initList) {
        initList.add("sub");
    }

    @onRender()
    public void render(@Param("renderList") List<String> renderList) {
        renderList.add("sub");
    }

    @onDestroy()
    public void destroy() {
        destroyList.add("sub");
    }


}
