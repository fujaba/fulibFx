package org.fulib.fx.app.controller.subcomponent.order;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnInit;
import org.fulib.fx.annotation.event.OnRender;
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

    @OnInit()
    public void init(@Param("initList") List<String> initList) {
        initList.add("othersubsub");
    }

    @OnRender()
    public void render(@Param("renderList") List<String> renderList) {
        renderList.add("othersubsub");
    }

    @OnDestroy()
    public void destroy() {
        destroyList.add("othersubsub");
    }

}
