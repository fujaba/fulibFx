package org.fulib.fx.app.controller.subcomponent.order;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import java.util.List;

@Component
public class OtherSubSubController extends VBox {

    @Param("destroyList")
    List<String> destroyList;

    @Inject
    public OtherSubSubController() {
    }

    @onInit()
    public void init(@Param("initList") List<String> initList) {
        initList.add("othersubsub");
    }

    @onRender()
    public void render(@Param("renderList") List<String> renderList) {
        renderList.add("othersubsub");
    }

    @onDestroy()
    public void destroy() {
        destroyList.add("othersubsub");
    }

}
